package com.example.solumonbackend.member.model;

import lombok.Builder;
import lombok.Getter;


public class KakaoStartDto {

  @Getter
  @Builder
  public static class Response {
    private boolean isMember;
    private String kakaoAccessToken;
  }
}
