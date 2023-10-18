package com.example.solumonbackend.global.exception;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<ErrorResponse> handleAccountException(MemberException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ResponseEntity<>(new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(TagException.class)
  public ResponseEntity<ErrorResponse> handleTagException(TagException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ResponseEntity<>(new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PostException.class)
  public ResponseEntity<ErrorResponse> handlePostException(PostException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ResponseEntity<>(new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotifyException.class)
  public ResponseEntity<ErrorResponse> handleNotifyException(NotifyException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ResponseEntity<>(new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(CustomSecurityException.class)
  public ResponseEntity<ErrorResponse> handleAccountException(CustomSecurityException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ResponseEntity<>(new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage()),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(SearchException.class)
  public ResponseEntity<ErrorResponse> handleTagException(SearchException e) {
    log.error("{} is occurred", e.getErrorCode());
    return new ResponseEntity<>(new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MailException.class)
  public ResponseEntity<ErrorResponse> handleMailException(MailException e) {
    log.error("{} is occurred", e.getMessage());
    return new ResponseEntity<>(new ErrorResponse("Failed", e.getErrorCode(), e.getErrorMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AmazonS3Exception.class)
  public ResponseEntity<ErrorResponse> handleAmazonS3Exception(AmazonS3Exception e) {
    log.error("{} is occurred", e.getMessage());
    return new ResponseEntity<>(
        new ErrorResponse("Failed", ErrorCode.AmazonS3Exception, e.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    log.error("{} is occurred", e.getMessage());
    return new ResponseEntity<>(
        new ErrorResponse("Failed", ErrorCode.IllegalArgumentException, e.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
    log.error("{} is occurred", e.getMessage());
    return new ResponseEntity<>(
        new ErrorResponse("Failed", ErrorCode.NullPointerException, e.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> MethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    BindingResult bindingResult = e.getBindingResult();

    StringBuilder sb = new StringBuilder();
    for (FieldError fieldError : bindingResult.getFieldErrors()) {
      sb.append(fieldError.getDefaultMessage()).append(" ");
    }
    // 테스트를 원할하게 하기 위해 바꾼 코드입니다. 나중에 수정 예정
    return new ResponseEntity<>(
        new ErrorResponse("Failed", ErrorCode.MethodArgumentNotValidException, sb.toString()),
        HttpStatus.BAD_REQUEST);
  }

}
