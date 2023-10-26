package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.type.ReportType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ReportDto {

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class Request {

    @NotNull
    private Long reportedMemberId;

    @NotNull
    private ReportType reportType;

    private String reportContent;

  }

}
