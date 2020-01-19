package com.castagnolofugale.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
            logger.info("#API Method|\n" + exchange.getRequest().getMethod()+"|"+exchange.getRequest().getURI()+'\n');
            logger.info("#API URI|\n"+exchange.getRequest().getURI()+'\n');
            logger.info("#Payload Input Size|\n"+ exchange.getRequest().getHeaders().getFirst("Content-Length")+'\n');



            Date date = new Date();
            long start = date.getTime();
            //numero richieste in 1s
            if(start - time >= rif){
                logger.info("#Count of requests in 1s|\n"+count+'\n');
                time = start;
                count = 1;
            }
            else count++;

            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                logger.info("#Payload Output Size|\n"+exchange.getResponse().getHeaders().getFirst("Content-Length")+'\n');

                Date date_t = new Date();

                logger.info("#Response Time in ms|\n"+(long)(date_t.getTime() - start)+'\n');

                logger.severe("#ERRORS\n");
            }));
        };
    }

}