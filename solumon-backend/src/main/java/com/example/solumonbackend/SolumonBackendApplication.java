package com.example.solumonbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableJpaAuditing
public class SolumonBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(SolumonBackendApplication.class, args);
  }

}
