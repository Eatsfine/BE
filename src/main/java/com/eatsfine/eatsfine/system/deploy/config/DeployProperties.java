package com.eatsfine.eatsfine.system.deploy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server")
public record DeployProperties(String profile) {
}
