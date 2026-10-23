package com.portal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    // root endpoint to prevent 404/500 when opening the backend domain directly
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> rootIndex() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Student Achievement Portal REST API is running.");
        response.put("healthCheck", "/api/health");
        return ResponseEntity.ok(response);
    }

    // test endpoint to verify backend connectivity and CORS from frontend
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Backend connected");
        return ResponseEntity.ok(response);
    }
}
