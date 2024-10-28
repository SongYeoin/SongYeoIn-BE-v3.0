package com.syi.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

  @GetMapping("/test")  // "/api/test" 경로로 GET 요청을 처리합니다.
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Hello from Spring Boot");
  }

}
