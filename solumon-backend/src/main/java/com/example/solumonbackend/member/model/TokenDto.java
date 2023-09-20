package com.example.solumonbackend.member.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TokenDto {

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    private String accessToken;
    private String refreshToken;
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {

    private String accessToken;
    private String refreshToken;
  }

}
