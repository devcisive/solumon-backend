package com.example.solumonbackend.member.type;

public enum ReportType {
  SEXUAL_CONTENT("음란성/선정성"),
  HARASSMENT("욕설/인신공격"),
  ADVERTISEMENT("광고"),
  OTHER("그 외 사유");


  private final String reportType;

  ReportType(String value) {
    this.reportType = value;
  }

  public String getValue(){
    return reportType;
  }
}
