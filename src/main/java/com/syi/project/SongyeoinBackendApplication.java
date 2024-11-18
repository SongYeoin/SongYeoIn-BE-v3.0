package com.syi.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SongyeoinBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(SongyeoinBackendApplication.class, args);
  }

}