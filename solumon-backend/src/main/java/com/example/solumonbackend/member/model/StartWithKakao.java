package com.example.solumonbackend.member.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StartWithKakao {

  @Getter
  @Builder
  public static class Response {
    private String kakaoAccessToken;
    private Boolean isMember;
  }
}
