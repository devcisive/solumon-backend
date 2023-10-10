package com.example.solumonbackend.global.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;




  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    return new KafkaAdmin(configs);
  }


  // 애플리케이션 로딩 시점에 카프카에 토픽을 등록할 수 있다.
  @Bean
  public NewTopic ChatTopicBuilder(){
    return TopicBuilder.name("chat").build(); // 파티션 등의 구성은 패스
  }


}
