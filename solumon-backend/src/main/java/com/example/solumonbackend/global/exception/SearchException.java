package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchException extends RuntimeException {

  private ErrorCode errorCode;
  private String errorMessage;

  public SearchException(ErrorCode errorCode) {
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }

}
