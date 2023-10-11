package com.example.solumonbackend.global.exception;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailException extends RuntimeException {

  private ErrorCode errorCode;
  private String errorMessage;

  public MailException(ErrorCode errorCode) {
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }
}
