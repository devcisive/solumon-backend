package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  MethodArgumentNotValidException("valid exception"),

  NOT_FOUND_MEMBER("해당 이메일로 등록된 사용자를 찾을 수 없습니다."),
  NOT_CORRECT_PASSWORD("비밀번호가 일치하지 않습니다."),
  ALREADY_EXIST_MEMBER("이미 존재하는 사용자 입니다."),
  ALREADY_REGISTERED_EMAIL("이미 등록된 이메일 입니다. 다른 이메일을 사용해 주세요"),
  ALREADY_REGISTERED_NICKNAME("이미 등록된 닉네임 입니다. 다른 닉네임을 사용해 주세요"),
  ALREADY_EXIST_USERNAME("닉네임이 중복됩니다. 다른 닉네임으로 설정해주세요"),

  INVALID_REFRESH_TOKEN("refresh token이 유효하지 않습니다.");

  private final String description;

}
