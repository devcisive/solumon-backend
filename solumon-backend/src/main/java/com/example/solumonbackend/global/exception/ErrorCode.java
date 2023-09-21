package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  NOT_FOUND_MEMBER("해당 사용자를 찾을 수 없습니다."),
  ALREADY_EXIST_MEMBER("이미 존재하는 사용자 입니다."),
  ALREADY_REGISTERED_EMAIL("이미 등록된 이메일 입니다."),
  ALREADY_EXIST_USERNAME("닉네임이 중복됩니다. 다른 닉네임으로 설정해주세요"),
  EMAIL_IS_REQUIRED("이메일 제공 항목에 동의해야 서비스를 이용할 수 있습니다."),
  PASSWORD_MUST_BE_LONGER_THAN_8_CHARACTERS("비밀번호는 8자 이상이어야 합니다."),
  PASSWORD_MUST_HAVE_SPECIAL_CHARACTER("비밀번호에는 1개 이상의 특수문자가 들어가야 합니다."),
  ACCESS_TOKEN_NOT_FOUND("유효하지 않은 로그인입니다. 재로그인 해주세요."),
  REFRESH_TOKEN_EXPIRED("로그인 유효기간이 지났습니다. 재로그인 해주세요.");

  private final String description;

}
