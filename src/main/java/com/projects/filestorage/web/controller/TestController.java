package com.projects.filestorage.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class TestController {


    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testMethod() {
        log.info("Principal: {}", SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("greeting", "Hello"));
    }

    @GetMapping("/forAll")
    public ResponseEntity<Map<String, String>> forAll() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", "For all"));
    }

}