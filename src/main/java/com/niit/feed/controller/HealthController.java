package com.niit.feed.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> hello() {
        return Map.of(
            "status", "ok",
            "message", "Feed System is running!"
        );
    }
}
