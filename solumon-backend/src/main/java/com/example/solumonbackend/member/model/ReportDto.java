package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.type.ReportType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
public class ReportDto {

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class Request {

    @NotNull
    private ReportType reportType;

    private String reportContent;

  }

}
