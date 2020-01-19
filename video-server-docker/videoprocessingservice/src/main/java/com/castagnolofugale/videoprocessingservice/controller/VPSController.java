package com.castagnolofugale.videoprocessingservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
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

        if(!new File(inputFile).exists()){
            return new ResponseEntity<>(false,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else{
            new File(outputPath).mkdirs();
            System.out.println("\nExecuting videprocessing.sh...\n");
            String[] cmd = { "/bin/bash", "-c", "/storage/var/script/videoprocessing.sh "+inputFile+" "+outputFile};
            Process p = Runtime.getRuntime().exec(cmd);
            System.out.println("Video processing finished\n");
            return new ResponseEntity<Boolean> (HttpStatus.OK);
        }

    }
}