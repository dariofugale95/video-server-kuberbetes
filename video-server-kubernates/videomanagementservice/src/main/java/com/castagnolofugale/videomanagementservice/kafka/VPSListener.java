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
                return repository.save(video);
            }).subscribe();
        }
    }
}
