package com.example.solumonbackend.member.model;

import lombok.Builder;
import lombok.Getter;


public class KakaoSignUpDto {

  @Getter
  @Builder
  public static class Response {
    private Long memberId;
    private Long kakaoId;
    private String email;
    private String nickname;

  }
}
