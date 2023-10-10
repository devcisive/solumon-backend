package com.example.solumonbackend.chat.service;


import com.example.solumonbackend.chat.entity.ChatMessage;
import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.chat.repository.ChatMessageRepository;
import com.example.solumonbackend.global.exception.ChatException;
import com.example.solumonbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaChatService {

    /* SimpMessagingTemplate)
   @EnableWebSocketMessageBroker를 통해서 등록되는 bean이다. 특정 Broker로 메시지를 전달한다.
   메시지를 클라이언트로 보내거나, 클라이언트로부터 메시지를 수신하는 데 사용되는 템플릿
   이 템플릿을 사용하여 채팅 메시지를 전송하거나 브로드캐스트한다.
   */

  private final SimpMessagingTemplate simpMessagingTemplate;

  private final KafkaTemplate<String, ChatMessage> kafkaChatMessageTemplate;
  private final String CHAT_TOPIC = "chat";


  private final ChatMessageRepository chatMessageRepository;


  /**
   * try-catch 문으로 시도
   */
  public void publishMessage(ChatMessage chatMessage) { // 특정 토픽에 메세지를 보낸다.

    try {
      kafkaChatMessageTemplate.send(CHAT_TOPIC, chatMessage);// 토픽, 키(파티션 여러개일때 위한 것), 데이터
      log.info(" postId : " + chatMessage.getPostId() + "chatMessage publish by " + chatMessage.getNickname());

    } catch (Exception e) { // 직렬화, 버퍼, 시간초과 등의 예외 등이 발생할 수 있는 듯하다.
      chatMessage.setSent(false);
      chatMessageRepository.save(chatMessage);

      throw new ChatException(ErrorCode.FAIL_SEND_MESSAGE);
    }
  }


//  /**
//   * addCallback 으로 시도 (비동기식?)
//   */
//  public void publishMessage(ChatMessage chatMessage) {
//
//      ListenableFuture<SendResult<String, ChatMessage>> future = kafkaChatMessageTemplate.send(CHAT_TOPIC, chatMessage);// 토픽, 키(파티션 여러개일때 위한 것), 데이터
//
//      future.addCallback(new KafkaSendCallback<String, ChatMessage>() {
//       // 프로듀서가 보낸 데이터의 브로커 적재 여부를 비동기로 확인
//       // void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback); 두 개다 인터페이스
//        @Override // 정상일 경우
//        public void onSuccess(SendResult<String, ChatMessage> result) {
//          log.info(" postId : " + chatMessage.getPostId() + "chatMessage publish by " + chatMessage.getNickname());
//        }
//
//        @Override // 이슈가 발생했을 경우
//        public void onFailure(KafkaProducerException ex) {
//          chatMessage.setSent(false);
//          chatMessageRepository.save(chatMessage);
//          throw new ChatException(ErrorCode.FAIL_SEND_MESSAGE);
//
//        }
//  });
//}



  @KafkaListener(topics = CHAT_TOPIC)   // 카프카의 특정 토픽에 온 이벤트를 처리
  public void consumeAndBroadcast(ChatMessage chatMessage) {
    Long postId = chatMessage.getPostId();


    try {
      //Client에서는 해당 주소를 SUBSCRIBE하고 있다가 이 주소에 메세지가 전달되면 화면에 출력당함.
      //convertAndSend(): 해당 주소로 받은 메세지를 해당 채팅방의 모든 구독자에게 브로드캐스트 (실시간 서버 -> 클라이언트 전달함으로써 실시간 대화 가능)
      simpMessagingTemplate.convertAndSend("/sub/chat/" + postId, ChatMessageDto.Response.chatMessageToResponse(chatMessage));
      log.info("consumed chatMessage for destination : /sub/chat/" + postId);

    } catch (Exception e) {
      chatMessage.setSent(false);
      chatMessageRepository.save(chatMessage);

      throw new ChatException(ErrorCode.FAIL_SEND_MESSAGE);
    }
  }


}
