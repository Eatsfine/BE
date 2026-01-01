package com.eatsfine.eatsfine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class EatsfineApplication {

    public static void main(String[] args) {
        SpringApplication.run(EatsfineApplication.class, args);
    }

}
