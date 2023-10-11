package com.example.solumonbackend.chat.model;

import com.example.solumonbackend.chat.entity.ChatMessage;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
public class ChatMessageDto {

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request{
    private String content;
  }


  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  public static class Response{
    private Long postId;
    private String nickname;
    private String contents;
    private LocalDateTime createdAt;

    public static ChatMessageDto.Response chatMessageToResponse(ChatMessage chatMessage){
       return Response.builder()
                 .postId(chatMessage.getPostId())
                 .nickname(chatMessage.getNickname())
                 .contents(chatMessage.getContents())
                 .createdAt(chatMessage.getCreatedAt())
                 .build();
    }


   public Response(){
     // 역직렬화 에러 뜨기 때문에 따로 만든 기본생성자
   }

  }

}
