package com.castagnolofugale.apigateway.controller;

import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @GetMapping(path = "/message")
    public String test(){
        return "[TEST] Sono API Gateway, la richiesta è arrivita fin qui!";
    }



}