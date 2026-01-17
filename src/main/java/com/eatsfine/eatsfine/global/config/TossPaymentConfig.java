package com.eatsfine.eatsfine.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Configuration
public class TossPaymentConfig {

    @Value("${payment.toss.widget-secret-key}")
    private String widgetSecretKey;

    @Bean
    public RestClient tossPaymentClient() {
        String encodedSecretKey = Base64.getEncoder().encodeToString((widgetSecretKey + ":").getBytes());

        return RestClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader("Authorization", "Basic " + encodedSecretKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
