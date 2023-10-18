package com.example.solumonbackend;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableBatchProcessing
@SpringBootApplication
//@SpringBootApplication(
//    exclude = {
//        org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration.class,
//        org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration.class,
//        org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration.class
//    }
//)
public class SolumonBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(SolumonBackendApplication.class, args);
  }

}
