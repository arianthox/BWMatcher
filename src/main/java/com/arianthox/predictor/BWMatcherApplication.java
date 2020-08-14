package com.arianthox.predictor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication(scanBasePackages = {"com.arianthox.predictor.commons","com.arianthox.predictor"})
public class BWMatcherApplication {
    public BWMatcherApplication(){

    }
    public static void main(String[] args) {
        SpringApplication.run(BWMatcherApplication.class, args);
    }
}
