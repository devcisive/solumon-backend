package com.example.solumonbackend.member.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LogOutDto {

  @Builder
  public static class Response {
    private Long memberId;
    private String status;
  }
}
