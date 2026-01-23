package com.eatsfine.eatsfine.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient businessWebClient(RestClient.Builder builder) {

        String baseUrl = "https://api.odcloud.kr/api/nts-businessman/v1";
        // 인코딩 문제 방지 설정
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return builder.uriBuilderFactory(factory)
                .baseUrl(baseUrl)
                .build();

    }
}
