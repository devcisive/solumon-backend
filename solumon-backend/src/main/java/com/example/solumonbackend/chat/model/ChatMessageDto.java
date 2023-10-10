package com.example.solumonbackend.chat.model;

import com.example.solumonbackend.chat.entity.ChatMessage;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
public class ChatMessageDto {

  @Getter
  @Setter
  public static class Request{
    private String content;
  }


  @Getter
  @Setter
  @Builder
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

  }

}
