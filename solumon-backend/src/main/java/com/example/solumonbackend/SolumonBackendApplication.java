package com.example.solumonbackend;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableBatchProcessing
@SpringBootApplication
@EnableScheduling
public class SolumonBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(SolumonBackendApplication.class, args);
  }

}
