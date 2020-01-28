package com.castagnolofugale.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Configuration
public class SpringCloudConfig {
    @Value(value = "${URLS}")
    private String urlRed;
    @Value(value = "${URi}")
    private String uri_videos;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value(value="${KAFKA_METRICS_TOPIC}")
    private String metricstopic;

    @Bean
    public RouteLocator apiGatewayRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/vms/**")
                        .filters(f->f.stripPrefix(1))
                        .uri(urlRed)
                        .id("vmsModule"))
                .route(r -> r.path("/videofiles")
                        .filters(f->f.stripPrefix(1))
                        .uri(uri_videos)
                        .id("storageModule"))
                .build();
    }

    //parametri per filtro
    private int count;
    private long time;
    private Logger logger  = Logger.getLogger("stats-metrics");
    private FileHandler handler;
    private static int rif=1000;

    public SpringCloudConfig() {
        this.count = 0;
        this.time = 0;

        try {
            handler = new FileHandler("/logs/metrics.log");
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Bean
    @Order(0)
    public GlobalFilter a(){
        return (exchange, chain) -> {
            //informazioni sull'api: metodo,uri, input size payload
            logger.info("#APIMethod|\n" + exchange.getRequest().getMethod()+"|"+exchange.getRequest().getURI()+'\n');
            logger.info("#APIURI|\n"+exchange.getRequest().getURI()+'\n');
            logger.info("#PayloadInputSize|\n"+ exchange.getRequest().getHeaders().getFirst("Content-Length")+'\n');



            Date date = new Date();
            long start = date.getTime();
            //numero richieste in 1s
            if(start - time >= rif){
                logger.info("#Countofrequestsin1s|\n"+count+'\n');
                kafkaTemplate.send(metricstopic, "RichiesteSecondo|" + count);
                time = start;
                count = 1;
            }
            else count++;

            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                logger.info("#PayloadOutputSize|\n"+exchange.getResponse().getHeaders().getFirst("Content-Length")+'\n');

                Date date_t = new Date();

                logger.info("#ResponseTimeinms|\n"+(long)(date_t.getTime() - start)+'\n');
                kafkaTemplate.send(metricstopic, "TempoRisposta|" + (long)(date_t.getTime() - start));

                logger.severe("#ERRORS\n");
            }));
        };
    }

}