package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.type.ReportSubject;
import com.example.solumonbackend.member.type.ReportType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ReportDto {

  @Builder
  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class Request {

    @NotNull
    private Long reportedMemberId;

    @NotNull
    private Long postId;

    @NotNull
    private ReportSubject reportSubject;

    private String reportTargetMessage; // ReportSubject가 CHAT일때만 사용

    @NotNull
    private ReportType reportType;

    private String reportExplanation;  // 신고타입이 other일 경우의 설명내용

  }

}
