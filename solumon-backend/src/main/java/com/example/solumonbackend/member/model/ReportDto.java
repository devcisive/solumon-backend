package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.type.ReportType;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class ReportDto {

  @Getter
  @Setter
  public static class Request {

    @NotNull
    private ReportType reportType;

    private String reportContent;

  }

}
