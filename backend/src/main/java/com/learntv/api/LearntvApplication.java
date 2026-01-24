package com.learntv.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LearntvApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearntvApplication.class, args);
    }
}
