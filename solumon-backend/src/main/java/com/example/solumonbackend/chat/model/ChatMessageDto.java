package com.example.solumonbackend.chat.model;

import lombok.AllArgsConstructor;
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

//  public static class Response{
//    private String senderNickname;
//    private String contents;
//    private String sentAt;
//
//  }

}
