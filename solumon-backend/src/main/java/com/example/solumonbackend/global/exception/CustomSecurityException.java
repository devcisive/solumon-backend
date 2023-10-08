package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomSecurityException extends RuntimeException{

  private ErrorCode errorCode;
  private String errorMessage;

  public CustomSecurityException(ErrorCode errorCode) {
    super(errorCode.getDescription());
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }
}
