package com.example.solumonbackend.global.exception;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MemberException.class)
  @MessageExceptionHandler(MemberException.class)
  public ErrorResponse handleAccountException(MemberException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(TagException.class)
  public ErrorResponse handleTagException(TagException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(PostException.class)
  public ErrorResponse handlePostException(PostException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(NotifyException.class)
  public ErrorResponse handleNotifyException(NotifyException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(CustomSecurityException.class)
  public ErrorResponse handleAccountException(CustomSecurityException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(SearchException.class)
  public ErrorResponse handleTagException(SearchException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(MailException.class)
  public ErrorResponse handleMailException(MailException e) {
    log.error("{} is occurred", e.getMessage());
    return new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage());
  }

  @ExceptionHandler(AmazonS3Exception.class)
  public ErrorResponse handleAmazonS3Exception(AmazonS3Exception e) {
    log.error("{} is occurred", e.getMessage());
    return new ErrorResponse("Failed", ErrorCode.AmazonS3Exception, e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
    log.error("{} is occurred", e.getMessage());
    return new ErrorResponse("Failed", ErrorCode.IllegalArgumentException, e.getMessage());
  }

  @ExceptionHandler(NullPointerException.class)
  public ErrorResponse handleNullPointerException(NullPointerException e) {
    log.error("{} is occurred", e.getMessage());
    return new ErrorResponse("Failed", ErrorCode.NullPointerException, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorResponse MethodArgumentNotValidException(MethodArgumentNotValidException e) {
    BindingResult bindingResult = e.getBindingResult();

    StringBuilder sb = new StringBuilder();
    for (FieldError fieldError : bindingResult.getFieldErrors()) {
      sb.append(fieldError.getDefaultMessage()).append(" ");
    }
    // 테스트를 원할하게 하기 위해 바꾼 코드입니다. 나중에 수정 예정
    return new ErrorResponse("Failed", ErrorCode.MethodArgumentNotValidException, sb.toString());
  }

}
