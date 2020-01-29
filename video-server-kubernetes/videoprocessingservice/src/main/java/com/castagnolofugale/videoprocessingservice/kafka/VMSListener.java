package com.castagnolofugale.videoprocessingservice.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class VMSListener {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_MAIN_TOPIC}")
    private String mainTopic;

    @KafkaListener(topics="${KAFKA_MAIN_TOPIC}")
    public void listen(String message) {
        String[] messageParts = message.split("\\|");

        if (messageParts[0].equals("process")) {
            String id = messageParts[1];
            processVideo(id);
        }
    }

    private void processVideo(String id){
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
            return;
        }
        else{
            new File(outputPath).mkdirs();
            String[] cmd = { "/bin/bash", "-c", "/storage/var/script/videoprocessing.sh"+" "+inputFile+" "+outputFile};
            try {
                Process p = Runtime.getRuntime().exec(cmd);
                kafkaTemplate.send(mainTopic,"processed|"+id);
            } catch (IOException e) {
                kafkaTemplate.send(mainTopic,"processingFailed|"+id);
                e.printStackTrace();
            }
            return;
        }
    }
}