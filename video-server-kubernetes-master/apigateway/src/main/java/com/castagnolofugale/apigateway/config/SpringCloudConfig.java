package com.castagnolofugale.apigateway.config;

import jdk.nashorn.internal.objects.Global;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.io.IOException;
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

    private int richieste;
    private long secondi;
    private Logger logger  = Logger.getLogger("stats");
    private FileHandler fh;

    public SpringCloudConfig() {
        this.richieste = 0;
        this.secondi = 0;

        try {
            fh = new FileHandler("/logs/stats.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
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
            logger.info("<--- API Richiesta --->\n" + exchange.getRequest().getMethod()+": "+exchange.getRequest().getURI());
            logger.info("<--- Payload Input Size --->\n"+ exchange.getRequest().getHeaders().getFirst("Content-Length"));

            Date date = new Date();
            long startTime = date.getTime();

            if(startTime - secondi >= 1000){
                secondi = startTime;
                logger.info("<--- Richieste al secondo --->\n"+richieste);
                richieste = 1;
            }
            else richieste++;

            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                logger.info("<--- Payload Output Size --->\n"+exchange.getResponse().getHeaders().getFirst("Content-Length"));

                Date ndate = new Date();
                long endTime = ndate.getTime() - startTime;
                logger.info("<--- Tempo di risposta --->\n"+endTime+" ms");
                // aggiungere un po' di roba
                logger.severe("<--- Errori ---->\n");
            }));
        };
    }
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
}