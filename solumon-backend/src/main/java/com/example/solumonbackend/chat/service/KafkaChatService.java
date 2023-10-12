package com.example.solumonbackend.chat.service;


import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaChatService {


  private final SimpMessagingTemplate simpMessagingTemplate; // 채팅메세지 전송 & 브로드캐스트

  private final KafkaTemplate<String, ChatMessageDto.Response> kafkaChatMessageTemplate;
  private final String CHAT_TOPIC = "chat";

  private final ChatMessageRepository chatMessageRepository;



  // 특정 토픽에 메세지를 보냄
  public void publishChatMessage(ChatMessageDto.Response chatMessage) {

    // ListenableFuture 은 콜백용(비동기식 작동)
    ListenableFuture<SendResult<String, ChatMessageDto.Response>> sendFuture = kafkaChatMessageTemplate.send(CHAT_TOPIC, chatMessage);

    sendFuture.addCallback(
        success -> {
      log.info("[토픽에 send 성공!]  partition: {}, offset: {}",
          success.getRecordMetadata().offset(), success.getRecordMetadata().partition());


    }, failure -> {
        log.error("[토픽에 send 실패!]  error msg: {}", failure.getMessage());
        chatMessageRepository.save(ChatMessageDto.Response.failChatMessageToEntity(chatMessage));
    });
    

    log.info(" postId : " + chatMessage.getPostId() + "chatMessage publish by " + chatMessage.getNickname());
  }





  // 카프카의 특정 토픽에 온 이벤트를 처리
  @KafkaListener(topics = CHAT_TOPIC, groupId = "chat-group", containerFactory = "chatKafkaListenerContainerFactory")
  public void consumeAndBroadcast(
      @Header(KafkaHeaders.ACKNOWLEDGMENT) Acknowledgment ack,
      @Header(value = KafkaHeaders.RECEIVED_MESSAGE_KEY, required = false) String key,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timeStamp,
      ChatMessageDto.Response chatMessage) {

      log.info("[Consumer] message: {}, key: {}, partition: {}, offset: {}, timeStamp: {}",
        chatMessage, key, partition, offset, timeStamp); //테스트용

//    if(true){
//      throw new RuntimeException("테스트용 예외던지기");
//    }

      //convertAndSend(): 해당 주소로 받은 메세지를 해당 채팅방의 모든 구독자에게 브로드캐스트 (실시간 서버 -> 클라이언트 전달함으로써 실시간 대화 가능)
      simpMessagingTemplate.convertAndSend("/sub/chat/" + chatMessage.getPostId(), chatMessage);


      ack.acknowledge(); // 해당 메세지를 잘 처리했다고 표시 (중복 처리 방지)
      chatMessageRepository.save(ChatMessageDto.Response.successChatMessageToEntity(chatMessage));
      log.info("브로드캐스팅 성공 + db에 저장 성공!");

  }


}
