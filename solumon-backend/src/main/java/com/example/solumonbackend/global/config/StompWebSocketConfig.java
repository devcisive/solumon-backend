package com.example.solumonbackend.global.config;

import com.example.solumonbackend.global.exception.StompExceptionHandler;
import com.example.solumonbackend.global.exception.StompHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@EnableWebSocketMessageBroker // Stomp 사용을 위한 어노테이션
@Configuration
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompHandler stompHandler;
  private final StompExceptionHandler stompExceptionHandler;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {

    // WebSocket가 웹소켓 핸드셰이크 커넥션을 생성할 경로
    // 이 엔드포인트로 웹소켓 연결을 할 수 있음 (소켓 연결 uri)
    registry.addEndpoint("/ws-stomp")
        .setAllowedOriginPatterns("*");

    registry.setErrorHandler(stompExceptionHandler);
    //소켓에서 예외발생 시 해당 핸들러로 제어권이 넘어감
  }

  // 메세지브로커: 메세지를 처리하고 클라이언트 간에 전달하는데 사용됨
  // Application 내부에서 사용할 path를 지정
  // MessageMapping 경로와 병합
  // 컨트롤러에서 만약  @MessageMapping(value = "/chat/enter") 이런식으로 돼있다면  -> /pub/chat/enter 이런식으로 되는 것
  @Override
  public void configureMessageBroker(final MessageBrokerRegistry registry) {

    registry.setApplicationDestinationPrefixes("/pub");
    // 메시지 발행 요청 prefix (Client에서의 SEND 요청을 처리)
    // 클라이언트가 해당 prefix가 붙은 메세지를 보낼 시 Broker로 보내짐
    // 해당 경로로 시작하는 STOMP 메세지의 "destination" 헤더는 @Controller 객체의 @MessageMapping 메서드로 라우팅된다.

    registry.enableSimpleBroker("/sub");
    // 메세지 구독 요청 prefix
    // 메세지를 받을 때의 경로를 설정
    // 해당 prefix가 붙은 경우 messageBroker가 해당 경로를 가로챈다.
    // SimpleBroker는 해당하는 경로를
    // SUBSCRIBE(구독)하는 Client에게 메세지를 전달하는 간단한 작업을 수행 (클라이언트가 메세지를 받는 상황)
  }


  // ClientInboundChannel: Client 에서 서버로 들어오는 요청을 전달하는 채널
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompHandler);

    /*
     메시지를 검증, 변환
     메시지 로깅
     인증 및 권한 부여
     비동기 작업 등의 로직을 ChannelInterceptor 등의 인터셉터를 구현한 클래스를 넣어주면 된다
    * */
  }

}
