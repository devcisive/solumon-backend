package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomSecurityException extends RuntimeException{

  private ErrorCode errorCode;
  private String errorMessage;

  public CustomSecurityException(ErrorCode errorCode) {
    super(errorCode.getDescription()); // 소켓연결하면서 인증 에러 터질 때 필요해서 추가
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }
}
