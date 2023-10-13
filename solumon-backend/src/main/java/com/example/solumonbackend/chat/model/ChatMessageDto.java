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
  @NoArgsConstructor
  public static class Response{
    private Long postId;
    private Long memberId;
    private String nickname;
    private String contents;
    private LocalDateTime createdAt;




    public static ChatMessage successChatMessageToEntity(ChatMessageDto.Response chatMessageResponse){
        return ChatMessage.builder()
            .postId(chatMessageResponse.getPostId())
            .memberId(chatMessageResponse.getMemberId())
            .nickname(chatMessageResponse.getNickname())
            .contents(chatMessageResponse.getContents())
            .createdAt(chatMessageResponse.getCreatedAt())
            .isSent(true)
            .build();
    }

    public static ChatMessage failChatMessageToEntity(ChatMessageDto.Response chatMessageResponse){
      return ChatMessage.builder()
          .postId(chatMessageResponse.getPostId())
          .memberId(chatMessageResponse.getMemberId())
          .nickname(chatMessageResponse.getNickname())
          .contents(chatMessageResponse.getContents())
          .createdAt(chatMessageResponse.getCreatedAt())
          .isSent(false)
          .build();
    }

  }

}
