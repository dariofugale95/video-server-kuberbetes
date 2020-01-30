package com.castagnolofugale.videomanagementservice.kafka;

import com.castagnolofugale.videomanagementservice.model.VideoInformation;
import com.castagnolofugale.videomanagementservice.model.VideoInformationStatus;
import com.castagnolofugale.videomanagementservice.repository.ReactiveVideoInformationRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class VPSListener {

    @Autowired
    ReactiveVideoInformationRepository repository;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Value(value = "${KAFKA_MAIN_TOPIC}")
    private String mainTopic;

    @KafkaListener(topics="${KAFKA_MAIN_TOPIC}")
    public void listen(String message) {

        String[] messageParts = message.split("\\|");




        if (messageParts[0].equals("processed")) {
            String videoId = messageParts[1];
            repository.findById(new ObjectId(videoId)).flatMap(video -> {
                video.setStatus(VideoInformationStatus.AVAILABLE);
                return repository.save(video);
            }).subscribe();
        }
        if (messageParts[0].equals("processingFailed")) {
            String videoId = messageParts[1];
            repository.findById(new ObjectId(videoId)).flatMap(video -> {
                video.setStatus(VideoInformationStatus.NOTAVAILABLE);

                String scriptPath = "/storage/var/script/";
                String scriptFile = scriptPath+"cleaner.sh";
                String command = "rm -rf /storage/var/video/"+video.get_id().toString()+" && rm -rf /storage/var/videofiles/"+video.get_id().toString();
                String[] cmd = { "/bin/bash", "-c", "/storage/var/script/cleaner.sh"};
                try {
                    FileWriter fileout = new FileWriter(scriptFile);
                    fileout.write(command);
                    fileout.write('\n');
                    fileout.close();
                    Process p1 = Runtime.getRuntime().exec("chmod u=rwx,g=rx,o=rx "+scriptFile);
                    Process p2 = Runtime.getRuntime().exec(cmd);
                } catch (IOException e) {
                    System.out.println(e);
                }

                return repository.save(video);
            }).subscribe();
        }
    }
}
