package com.example.solumonbackend.global.exception;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Slf4j
@Component
public class StompExceptionHandler extends StompSubProtocolErrorHandler {

  // 여기서 예외를 받게될 때  message의 isSent가 false인 데이터를 저장하려고 했는데 순환참조 돼서 사용할 수 없다.
  // private ChatService chatService;


  public StompExceptionHandler() {
    super();
  }


  @Override // 소켓에서 예외발생 시 실행됨
  public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage,
      Throwable ex) {

    // Throwable 로 들어온 객체가 실제로 발생한 예외가 맞아도 instance of 로 캐치가 안됨
    // 무조건 MessageDeliveryException 로 캐치되는 상태
    if (ex instanceof MessageDeliveryException) {
      return handleUnauthorizedException(ex);
    }

    return super.handleClientMessageProcessingError(clientMessage, ex);
  }


  // handlerClientMessageProcessingError( ) 를 호출하고,
  // 처리가 끝나면 HandlerInternal( )를 마지막으로 호출해 메세지를 전송하는 흐름
  @Override
  protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor,
      byte[] errorPayload, Throwable cause, StompHeaderAccessor clientHeaderAccessor) {

    return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());

  }


  private Message<byte[]> handleUnauthorizedException(Throwable ex) {
    return prepareErrorMessage(ex.getMessage());
  }


  private Message<byte[]> prepareErrorMessage(String errorMessage) {

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
    accessor.setMessage(errorMessage);
    accessor.setLeaveMutable(true); // 헤더 정보 변경 허용

    return MessageBuilder.createMessage(errorMessage.getBytes(StandardCharsets.UTF_8),
        accessor.getMessageHeaders());
  }


}
