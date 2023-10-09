package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.type.ReportType;
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
  public static class Request {

    @NotNull
    private ReportType reportType;

    private String reportContent;

  }

}
