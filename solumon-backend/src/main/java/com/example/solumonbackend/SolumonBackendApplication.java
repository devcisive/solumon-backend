package com.example.solumonbackend;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableBatchProcessing
@SpringBootApplication

public class SolumonBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(SolumonBackendApplication.class, args);
  }

}
