package com.example.solumonbackend.member.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LogOutDto {

  @Getter
  @Builder
  public static class Response {
    private Long memberId;
    private String status;
  }
}
