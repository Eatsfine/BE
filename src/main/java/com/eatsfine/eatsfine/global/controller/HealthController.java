package com.eatsfine.eatsfine.global.controller;

import com.eatsfine.eatsfine.global.config.DeployProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final DeployProperties deployProperties;

    public HealthController(DeployProperties deployProperties) {
        this.deployProperties = deployProperties;
    }

    @GetMapping("/api/v1/deploy/health-check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok(deployProperties.profile());
    }
}
