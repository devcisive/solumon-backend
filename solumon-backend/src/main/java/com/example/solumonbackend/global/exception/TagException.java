package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class TagException extends RuntimeException {

  private ErrorCode errorCode;
  private String errorMessage;


  public TagException(ErrorCode errorCode, String tagName) {
    this.errorCode = errorCode;
    this.errorMessage = tagName + ": "+ errorCode.getDescription();
  }
}
