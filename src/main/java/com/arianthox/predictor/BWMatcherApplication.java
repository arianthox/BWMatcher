package com.arianthox.predictor;

import com.arianthox.predictor.hmm.EngineManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import java.util.ArrayList;

@EnableEurekaClient
@SpringBootApplication
public class BWMatcherApplication {
    public BWMatcherApplication(){
        EngineManager manager=new EngineManager();
        manager.trainKey("test",new ArrayList<>());
    }
    public static void main(String[] args) {
        SpringApplication.run(BWMatcherApplication.class, args);
    }
}
