package com.example.solumonbackend.global.config;

import com.example.solumonbackend.chat.entity.ChatMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;


//  @Value(value = "${kafka.consumer.group}")
//  private final String groupId;



  // Kafka ConsumerFactory를 생성하는 Bean 메서드
  @Bean
  public ConsumerFactory<String, ChatMessage> consumerFactory() { // 인자를 넣기도 하고 아니기도 하고(근데 안되네)
    JsonDeserializer<ChatMessage> deserializer = new JsonDeserializer<>();
    // 패키지 신뢰 오류로 인해 모든 패키지를 신뢰하도록 작성
    deserializer.addTrustedPackages("*");

    //
    deserializer.setRemoveTypeHeaders(false);
    deserializer.addTrustedPackages("*");
    deserializer.setUseTypeMapperForKey(true);

//위에껀 뭔지 잘..

    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress); // 카프카브로커 설정 여러개 설정하는것 권장
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-group"); // 그룹 아이디는 컨슈머 그룹이라고도 함(다른 컨슈머 그룹에 속한 컨슈머들은 서로 영향 X)
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); //흠.. 이거 맞나..?

    // 이전 메세지를 필요로 하거나 중복 메시지를 감수할 수 있다면 earliest
    // 현재 이후의 메시지만 필요하다면 latest
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

    return new DefaultKafkaConsumerFactory<>(consumerProps);
  }


  // KafkaListener 컨테이너 팩토리를 생성하는 Bean 메서드
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, ChatMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());

    return factory;
  }



  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> chatKafkaListenerContainerFactory() {
    return kafkaListenerContainerFactory();
  }

}
