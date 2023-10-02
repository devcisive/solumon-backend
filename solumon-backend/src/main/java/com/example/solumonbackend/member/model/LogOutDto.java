package com.example.solumonbackend.member.model;

import lombok.Builder;
import lombok.Getter;

public class LogOutDto {

  @Getter
  @Builder
  public static class Response {
    private Long memberId;
    private String status;
  }
}
