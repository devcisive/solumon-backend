package com.example.solumonbackend.member.model;

import lombok.Builder;
import lombok.Getter;


public class KakaoSignInDto {

  @Getter
  @Builder
  public static class Response {
    private Long memberId;
    private boolean isFirstLogin;
    private String accessToken;
    private String refreshToken;
  }
}
