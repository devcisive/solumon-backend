package com.example.solumonbackend.member.model;

import lombok.Builder;
import lombok.Getter;

public class KakaoTokenUpdateDto {

  @Getter
  @Builder
  public static class Response {
    private Long memberId;
    private String accessToken;
    private String refreshToken;
  }
}
