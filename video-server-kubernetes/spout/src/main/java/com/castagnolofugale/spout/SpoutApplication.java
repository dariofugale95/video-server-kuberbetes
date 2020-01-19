package com.castagnolofugale.spout;

import com.castagnolofugale.spout.kafka.KafkaStreamer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpoutApplication {

    public static void main(String[] args) {
        new Thread(new KafkaStreamer()).start();
    }

}
