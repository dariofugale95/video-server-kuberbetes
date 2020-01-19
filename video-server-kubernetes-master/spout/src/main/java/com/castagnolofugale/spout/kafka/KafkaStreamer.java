package com.castagnolofugale.spout.kafka;

import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.*;

@Service
public class KafkaStreamer implements Runnable{
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value="${KAFKA_MAIN_TOPIC}")
    private String maintopic;

    @Override
    public void run() {
        try{
            insertMetrics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertMetrics() {
        while(true){
            System.out.println("Saving metrics/stats on kafka...\n");
            String statsFile = "/logs/stats.log";

            if (!new File(statsFile).exists()) {
                System.out.println("There aren't statistics...\n");
                return;
            } else {
                //FileReader file_stats = null;
                try {
                    FileReader file_stats = new FileReader(statsFile);
                    BufferedReader buffer = new BufferedReader(file_stats);
                    String nextStr;
                    String value;
                    try {
                        nextStr = buffer.readLine();// legge una riga del file
                        while (nextStr != null) {
                            System.out.println(nextStr);  // visualizza la riga
                            if (nextStr.equals("INFO: <--- Tempo di risposta --->")) {
                                System.out.println("Precedente" +nextStr);
                                nextStr = buffer.readLine();
                                value = nextStr;
                                System.out.println("Successivo" +value);
                                kafkaTemplate.send(maintopic, "TempoRisposta|" + value);

                            }
                            if (nextStr.equals("INFO: <--- Richieste al secondo --->")) {
                                System.out.println("Caso 2 Precedente" +nextStr);
                                nextStr = buffer.readLine();
                                value = nextStr;
                                System.out.println("Caso 2 Successivo" +value);
                                kafkaTemplate.send(maintopic, "RichiesteSecondo|" + value);

                            }
                            nextStr = buffer.readLine(); // legge la prossima riga
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
