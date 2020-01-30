package com.castagnolofugale.videoprocessingservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;

@RestController
public class VPSController {
    // ------------- TEST ---------------------
    @GetMapping(path = "/message")
    public String test() {
        return "[TEST] Sono il Video Processing Service, la richiesta è arrivita fin qui!";
    }

    @PostMapping(path="/videos/process")
    public @ResponseBody
    ResponseEntity<Boolean> process(@RequestBody Map<String,String> payload) throws IOException {

        if(!payload.containsKey("videoId")) return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        String id = String.valueOf(payload.get("videoId"));

        String path = "/storage/var/video/" + id + "/";
        String inputFile=path+"video.mp4";
        String outputPath = "/storage/var/videofiles/" + id + "/";
        String outputFile = outputPath+"video.mpd";

        String scriptPath = "/storage/var/script/";
        String scriptFile = scriptPath+"videoprocessing.sh";

        if(!new File(scriptPath).exists()){
            new File(scriptPath).mkdirs();
            try {
                FileWriter fileout = new FileWriter(scriptFile);
                String command = "ffmpeg -i \"$1\" -map 0:v:0 -map 0:a?:0 -map 0:v:0 -map 0:a?:0 -map 0:v:0 -map 0:a?:0 -map 0:v:0 -map 0:a?:0 -map 0:v:0 -map 0:a?:0 -map 0:v:0 -map 0:a?:0 -b:v:0 350k  -c:v:0 libx264 -filter:v:0 \"scale=320:-2\" -b:v:1 1000k -c:v:1 libx264 -filter:v:1 \"scale=640:-2\" -b:v:2 3000k -c:v:2 libx264 -filter:v:2 \"scale=1280:-2\" -use_timeline 1 -use_template 1 -window_size 6 -adaptation_sets \"id=0,streams=v  id=1,streams=a\" -f dash \"$2\" ";
                fileout.write(command);
                fileout.write('\n');
                fileout.close();
                Process p = Runtime.getRuntime().exec("chmod u=rwx,g=rx,o=rx "+scriptFile);

            } catch (IOException e) {
                System.out.println(e);
            }
        }


        if(!new File(inputFile).exists()){
            return new ResponseEntity<>(false,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else{
            new File(outputPath).mkdirs();
            System.out.println("\nExecuting videprocessing.sh...\n");
            String[] cmd = { "/bin/bash", "-c", "/storage/var/script/videoprocessing.sh"+" "+inputFile+" "+outputFile};
            Process p = Runtime.getRuntime().exec(cmd);
            System.out.println("Video processing finished\n");
            return new ResponseEntity<Boolean> (HttpStatus.OK);
        }

    }
}