package com.example.solumonbackend.member.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoSignInDto {
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Request {
    private String kakaoAccessToken;
  }

  @Getter
  @Builder
  public static class Response {
    private Long memberId;
    private Boolean isFirstLogIn;
    private String accessToken;
    private String refreshToken;
  }
}
