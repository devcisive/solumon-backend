package com.example.solumonbackend.global.exception;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostException extends RuntimeException {

  private ErrorCode errorCode;
  private String errorMessage;

  public PostException(ErrorCode errorCode) {
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }
}
