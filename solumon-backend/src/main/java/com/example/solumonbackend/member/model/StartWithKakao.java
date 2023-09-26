package com.example.solumonbackend.member.model;

import lombok.Builder;
import lombok.Getter;

public class StartWithKakao {

  @Getter
  @Builder
  public static class Response {
    private String kakaoAccessToken;
    private Boolean isMember;
  }
}
