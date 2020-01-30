package com.castagnolofugale.videomanagementservice.controller;

import com.castagnolofugale.videomanagementservice.model.User;
import com.castagnolofugale.videomanagementservice.model.VideoInformation;
import com.castagnolofugale.videomanagementservice.model.VideoInformationStatus;
import com.castagnolofugale.videomanagementservice.repository.ReactiveVideoInformationRepository;
import com.castagnolofugale.videomanagementservice.service.VMSUserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class VMSController {

    @Autowired
    ReactiveVideoInformationRepository repository;
    @Autowired
    VMSUserService userService;
    @Autowired
    HttpServletRequest request;

    @Value(value = "${URLS}")
    private String urlVPS;

    // ------------- TEST ---------------------
    @GetMapping(path = "/message")
    public String test() {
        return "[TEST] Sono il Video Management Service, la richiesta è arrivita fin qui!";
    }


    // ------------- VIDEOS -------------------

    // POST: /videos questa API serve a creare un'entry nel db con le informazioni del video che sono state inserite dall'utente.
    @PostMapping(value = "/videos", consumes = "application/JSON")
    public Mono<VideoInformation> newVideoInformation(@RequestBody VideoInformation videoInformation, Authentication auth){
        //nel db il al video con l'id in questione viene settato lo stato WAITINGUPOLOAD
        videoInformation.setStatus(VideoInformationStatus.WAITINGUPLOAD);
        // viene tenuta traccia dell'utente che ha inserito le info riguardo al video
        videoInformation.setUser(auth.getName());
        //salvataggio
        return repository.save(videoInformation);
    }

    // POST: /videos/:id questa API serve a fare l'upload del video con l'id che viene passato
    @PostMapping(value = "/videos/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<VideoInformation>  uploadNewVideo(@RequestBody MultipartFile videoFile, @PathVariable String id){
        System.out.println("Saving video.mp4 in storage...\n");
        String videosPath = "/storage/var/video/" + id + "/";

        Mono<VideoInformation> info;
        info = repository.findById(new ObjectId(id));
        String status;
        status = info.block().getStatus().toString();
        if(info.block()==null || !(status.equals("WAITINGUPLOAD"))) {
            return info;
        }

        //si verifica se già esiste la cartella con l'id video passato
        if(!new File(videosPath).exists()){
            new File(videosPath).mkdirs();
        }
        try {
            //caricamento del video
            byte[] bytes = videoFile.getBytes();
            Path path = Paths.get(videosPath+"video.mp4");
            Files.write(path,bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendJsonToVPS(id);


        VideoInformation video = repository.findById(new ObjectId(id)).block();

        //lo stato viene settato in UPLOADED
        video.setStatus(VideoInformationStatus.UPLOADED);

        return repository.save(video);
    }

    // GET: /videos questa API restituisce tutte le entry presenti nel db, quindi tutti gli oggetti video con i loro attributi
    @GetMapping(path = "/videos")
    public Flux<VideoInformation> getVideoInformations(){
        return repository.findAll();
    }


    // -------------- USER ---------------------

    // POST: /register
    @PostMapping(path = "/register")
    public @ResponseBody
    Mono<User> register(@RequestBody User user){
        return userService.addUser(user);
    }

    // GET: /users/all
    @GetMapping(path = "/users/all")
    public @ResponseBody
    Flux<User> getAll(){
        return userService.getAllUsers();
    }

    // GET: /users/:username
    @GetMapping(path = "/users/{username}")
    public @ResponseBody Mono<User> getUser(Authentication auth, @PathVariable String username){
        if(username.equalsIgnoreCase(auth.getName()))
            return userService.getByUsername(username);
        else return null;
    }

    // DELETE: /users/:id
    @DeleteMapping(path = "/users/{_id}")
    public @ResponseBody String deleteUser(@PathVariable ObjectId _id){
        return userService.deleteUser(_id);
    }
    // GET: /videos/:id questa API permette il dowload del video in formato mpd

    @GetMapping(path = "/videos/{id}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody public ResponseEntity getPreview1(@PathVariable String id, HttpServletResponse response) {
        ResponseEntity result = null;
        Mono<VideoInformation> info;
        info = repository.findById(new ObjectId(id));
        if (info.block() == null) {
            System.out.println("Il video non è stato trovato");
            result = new ResponseEntity(null, null, HttpStatus.NOT_FOUND);
        } else {
            if (!(info.block().getStatus().equals("AVAILABLE"))) {
                result = new ResponseEntity(null, null, HttpStatus.NOT_FOUND);

            } else {
                try {
                    System.out.println("Il video è stato trovato,lo sto scaricando");
                    String path = "/storage/var/videofiles/" + id + "/video.mpd";
                    byte[] video = Files.readAllBytes(Paths.get(path));


                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));
                    headers.setContentLength(video.length);
                    result = new ResponseEntity(video, headers, HttpStatus.OK);
                    //"redirect:/var/videofiles/"+id+"/video.mpd", HttpStatus.MOVED_PERMANENTLY
                    response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
                } catch (java.nio.file.NoSuchFileException e) {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                } catch (Exception e) {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

/*

// GET: /videos/:id
    @GetMapping(path = "/videos/{id}")
    public @ResponseBody
    ResponseEntity<String> getVideo(@PathVariable String id){
        Mono<VideoInformation> info;

        info=repository.findById(new ObjectId(id));
        if(info.block()==null){
            return new ResponseEntity<>(
                    "Non trovata", HttpStatus.NOT_FOUND
            );
        }
        else{
            return new ResponseEntity<>(
                    "redirect:/var/videofiles/"+id+"/video.mpd", HttpStatus.MOVED_PERMANENTLY
            );
        }
    }
 */



    private void sendJsonToVPS(String id){
        // request url
        String url = "http://videoprocessingservice:8085/videos/process";
        // crea istanza di RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // crea headers
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, String> map = new HashMap<>();
        map.put("videoId", id);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers);
        // viene inviata la POST
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    }
}