package com.example.solumonbackend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MemberException.class)
  public ErrorResponse handleAccountException(MemberException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(CustomSecurityException.class)
  public ErrorResponse handleAccountException(CustomSecurityException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorResponse MethodArgumentNotValidException(MethodArgumentNotValidException e) {
    BindingResult bindingResult = e.getBindingResult();

    StringBuilder sb = new StringBuilder();
    for (FieldError fieldError: bindingResult.getFieldErrors()) {
      sb.append(fieldError.getDefaultMessage()).append(" ");
    }
    // 테스트를 원할하게 하기 위해 바꾼 코드입니다. 나중에 수정 예정
    return new ErrorResponse("Failed", ErrorCode.MethodArgumentNotValidException, sb.toString());
  }
}
