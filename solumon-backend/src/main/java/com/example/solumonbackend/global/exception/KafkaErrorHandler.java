package com.example.solumonbackend.global.exception;

import com.example.solumonbackend.chat.model.ChatMessageDto.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaErrorHandler implements KafkaListenerErrorHandler { // 이건 만드는 중(지금은 잘 돌아가서 후순위)

  private final KafkaTemplate<String, Response> kafkaChatMessageTemplate;


  @Override
  public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
    return null;
  }

  @Override
  public Object handleError(Message<?> message, ListenerExecutionFailedException exception,
      Consumer<?, ?> consumer) {
    return KafkaListenerErrorHandler.super.handleError(message, exception, consumer);
  }
}
