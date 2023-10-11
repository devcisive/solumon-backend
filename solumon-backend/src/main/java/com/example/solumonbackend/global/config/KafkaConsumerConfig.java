package com.example.solumonbackend.global.config;

import com.example.solumonbackend.chat.entity.ChatMessage;
import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.global.exception.ChatException;
import com.example.solumonbackend.global.exception.KafkaErrorHandler;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;



  // Kafka ConsumerFactory를 생성하는 Bean 메서드
  @Bean
  public ConsumerFactory<String, ChatMessageDto.Response> consumerFactory() {
    JsonDeserializer<ChatMessage> deserializer = new JsonDeserializer<>();
    // 패키지 신뢰 오류로 인해 모든 패키지를 신뢰하도록 작성
//    deserializer.addTrustedPackages("*");
//
//    deserializer.setRemoveTypeHeaders(false);
//    deserializer.addTrustedPackages("*");
//    deserializer.setUseTypeMapperForKey(true);


    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress); // 카프카브로커 설정 여러개 설정하는것 권장
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-group"); // 그룹 아이디는 컨슈머 그룹이라고도 함(다른 컨슈머 그룹에 속한 컨슈머들은 서로 영향 X)

    // 이전 메세지를 필요로 하거나 중복 메시지를 감수할 수 있다면 earliest
    // 현재 이후의 메시지만 필요하다면 latest
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");


    // 이걸로 하니까 역직렬화 에러발생 (에러핸들링 테스트 때문에 지우지는 않습니다.)
//    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//    return new DefaultKafkaConsumerFactory<>(consumerProps);


    return new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new JsonDeserializer<>(ChatMessageDto.Response.class));
  }


  // KafkaListener 컨테이너 팩토리를 생성하는 Bean 메서드
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto.Response> chatKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto.Response> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setCommonErrorHandler(customErrorHandler());


    return factory;
  }



  
  // 역직렬화 안될때 무한으로 시도하는 것 막으려고 했는데 안먹히는 중
  // 지금은 설정을 바꿔서 역직렬화안되는 문제는 해결되기는 한 상태라서 이 핸들러 만드는 것은 후순위
  @Bean
  public DefaultErrorHandler customErrorHandler()  {
    DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
      log.error("[Error] topic = {}, key = {}, value = {}, error message = {}", consumerRecord.topic(),
          consumerRecord.key(), consumerRecord.value(), exception.getMessage());
    }, new FixedBackOff(0, 0)); // 재시도 간격 0, 재시도 횟수 0
    errorHandler.addNotRetryableExceptions(ChatException.class); // 해당 예외로 발생했을 땐 재시도 X

    return errorHandler;
  }

}
