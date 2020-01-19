package com.castagnolofugale.videomanagementservice.controller;

import com.castagnolofugale.videomanagementservice.model.User;
import com.castagnolofugale.videomanagementservice.model.VideoInformation;
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

    // POST: /videos
    @PostMapping(value = "/videos", consumes = "application/JSON")
    public Mono<VideoInformation> newVideoInformation(@RequestBody VideoInformation videoInformation){
        return repository.save(videoInformation);
    }

    // POST: /videos/:id
    @PostMapping(value = "/videos/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadNewVideo(@RequestBody MultipartFile videoFile, @PathVariable String id){
        System.out.println("Saving video.mp4 in storage...\n");
        String videosPath = "/storage/var/video/" + id + "/";
        if(!new File(videosPath).exists()){
            new File(videosPath).mkdirs();
        }
        try {
            byte[] bytes = videoFile.getBytes();
            Path path = Paths.get(videosPath+"video.mp4");
            Files.write(path,bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendJsonToVPS(id);
        return "Video upload correctly";
    }

    // GET: /videos
    @GetMapping(path = "/videos")
    public Flux<VideoInformation> getVideoInformations(){
        return repository.findAll();
    }

    // GET: /videos/:id
    @GetMapping(path = "/videos/{id}")
    public Mono<VideoInformation> getVideoInformation(@PathVariable String id){
        return repository.findById(new ObjectId(id));
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

  @GetMapping(path = "/{id}")
    public @ResponseBody
     ResponseEntity<String> getVideo(@PathVariable String id){
         Mono<VideoInformation> info;
         
         info=repository.findById(id);
         if(info.block()==null){
             return new ResponseEntity<>(
                     "Non trovata", HttpStatus.NOT_FOUND
             );
         }
        else{
             return new ResponseEntity<>(
                     "redirect:/var/videofiles/"+id+"/video.mpd", HttpStatus.MOVED_PERMANENTLY);
           }
 
  
    }
    
    private void sendJsonToVPS(String id){
        // request url
        String url = "http://videoprocessingservice:8085/videos/process";
        // create an instance of RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // create headers
        HttpHeaders headers = new HttpHeaders();
        // set `content-type` header
        headers.setContentType(MediaType.APPLICATION_JSON);
        // set `accept` header
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // request body parameters
        Map<String, String> map = new HashMap<>();
        map.put("videoId", id);
        // build the request
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers);
        // send POST request
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    }
}
