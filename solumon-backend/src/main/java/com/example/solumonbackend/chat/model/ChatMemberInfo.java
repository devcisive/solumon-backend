package com.example.solumonbackend.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemberInfo {

  private Long memberId;
  private String nickname;

}
