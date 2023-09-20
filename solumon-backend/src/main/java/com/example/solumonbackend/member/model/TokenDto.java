package com.example.solumonbackend.member.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TokenDto {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    private String accessToken;
    private String refreshToken;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {

    private String accessToken;
    private String refreshToken;
  }

}
