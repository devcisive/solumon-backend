package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatException extends RuntimeException {

  private ErrorCode errorCode;
  private String errorMessage;

  public ChatException(ErrorCode errorCode) {
    super(errorCode.getDescription());
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }
}
