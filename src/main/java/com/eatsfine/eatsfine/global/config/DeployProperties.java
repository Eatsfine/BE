package com.eatsfine.eatsfine.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server")
public record DeployProperties(String profile) {
}
