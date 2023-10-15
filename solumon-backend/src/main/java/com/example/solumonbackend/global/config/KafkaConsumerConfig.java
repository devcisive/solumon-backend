package com.example.solumonbackend.global.config;

import com.example.solumonbackend.chat.entity.ChatMessage;
import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.chat.model.ChatMessageDto.Response;
import com.example.solumonbackend.chat.repository.ChatMessageRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;



@Slf4j
@RequiredArgsConstructor
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;


  private final KafkaTemplate<String, Response> kafkaChatMessageTemplate;
  private final ChatMessageRepository chatMessageRepository;


  // consumer 설정값
  @Bean
  public DefaultKafkaConsumerFactory<String,ChatMessageDto.Response> consumerFactory() {
    JsonDeserializer<ChatMessageDto.Response> jsonDeserializer = new JsonDeserializer<>(ChatMessageDto.Response.class);


    jsonDeserializer.addTrustedPackages("*"); // 패키지 신뢰 오류로 인해 모든 패키지를 신뢰하도록 작성
    jsonDeserializer.setRemoveTypeHeaders(false); // Kafka 메시지를 역직렬화할 때 타입 헤더가 메시지에 유지
    jsonDeserializer.setUseTypeMapperForKey(true);  // Class Name Not Fond 예외방지


    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress); // 카프카브로커 설정 여러개 설정하는것 권장
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-group"); // 그룹 아이디는 컨슈머 그룹이라고도 함(다른 컨슈머 그룹에 속한 컨슈머들은 서로 영향 X)


    // 이전 메세지를 필요로 하거나 중복 메시지를 감수할 수 있다면 earliest
    // 현재 이후의 메시지만 필요하다면 latest
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 데이터베이스에 메세지를 저장 후 커밋을 하기 위함


    return new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(),
        new ErrorHandlingDeserializer<>(jsonDeserializer));


  }



  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto.Response> chatKafkaListenerContainerFactory() {

    ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto.Response> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());

    factory.setCommonErrorHandler(customErrorHandler());
    factory.setConcurrency(1);  // 임시 (사용할 스레드의 개수)
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // ack.acknowledge(); 까지 돼야 다음 데이터를 처리 (수신에 성공했다고 명시적으로 호출해줘야함)

    return factory;
  }



  @Bean
  public DefaultErrorHandler customErrorHandler()  {
    DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
      log.error("[메세지 전송 실패] topic = {}, key = {}, value = {}, error message = {}",
          consumerRecord.topic(), consumerRecord.key(), consumerRecord.value(), exception.getMessage());

      ChatMessage chatMessage
          = Response.failChatMessageToEntity((ChatMessageDto.Response) consumerRecord.value());
      chatMessageRepository.save(chatMessage);

      }, new FixedBackOff(2, 3)); // 재시도 간격 2초, 재시도 횟수 3번 (임시)


    return errorHandler;
  }

}
