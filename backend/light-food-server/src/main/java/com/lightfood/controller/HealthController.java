package com.lightfood.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public liveness endpoint for browser/manual checks.
 */
@RestController
public class HealthController {

    @GetMapping({"/", "/api/health"})
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
