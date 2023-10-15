package com.example.solumonbackend.global.config;

import com.example.solumonbackend.chat.model.ChatMessageDto;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@EnableKafka
@Configuration
public class KafkaProducerConfig { // 데이터를 카프카의 토픽에 보내는 역할

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;


  @Bean
  public ProducerFactory<String, ChatMessageDto.Response> producerFactory() {

    Map<String, Object> producerProps = new HashMap<>();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);  // 부트스트랩이 로컬호스트의 카프카를 바라보도록 함
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);    // 키는 메세지를 보내면, 토픽의 파티션이 지정될 때 사용됨
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return new DefaultKafkaProducerFactory<>(producerProps);

  }




  // 카프카로 메세지를 보내려면 KafkaOperations 인터페이스를 구현한 객체가 필요 (KafkaTemplate)
  // KafkaTemplate을 생성하는 Bean 메서드 (Key, Value) 위의 설정과 관련있음
  // 정의된 설정을 바탕으로 실질적인 동작을 하게 해줌
  @Bean
  public KafkaTemplate<String, ChatMessageDto.Response> kafkaChatMessageTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
