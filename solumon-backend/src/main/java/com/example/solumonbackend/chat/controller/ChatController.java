package com.example.solumonbackend.chat.controller;

import com.example.solumonbackend.chat.model.ChatMemberInfo;
import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.chat.service.ChatService;
import com.example.solumonbackend.chat.service.RedisChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

  private final ChatService chatService;
  private final RedisChatService redisChatService;


  // 해당 경로로 메세지가 도착했을때 해당 메소드가 호출
  //stompConfig에서 설정한 destinationPrefixes 와 @MessageMapping 경로가 병합됨 ("/pub/chat/{postId}")
  @MessageMapping("/chat/{postId}") // 클라이언트의 요청을 처리
  @SendTo("/sub/chat/{postId}")  // 처리한 결과를 다시 클라이언트에게 전달
  public ResponseEntity<Void> sendMessage(
      Message<?> message,
      @Payload ChatMessageDto.Request request,
      @DestinationVariable(value = "postId") long postId) {

    ChatMemberInfo chatMemberInfo
        = redisChatService.getChatMemberInfo(
        StompHeaderAccessor.wrap(message).getSessionId()); //처음 커넥했을 당시에 세션아이디를 키로 저장했던 유저 정보를 가져옴

    chatService.sendAndSaveChatMessage(postId, request, chatMemberInfo);
    return ResponseEntity.ok().build();
  }

}
