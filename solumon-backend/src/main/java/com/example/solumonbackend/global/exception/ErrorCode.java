package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  MethodArgumentNotValidException("valid exception"),
  INPUT_INVALID("입력값이 올바르지 않습니다."),

  NOT_FOUND_MEMBER("해당 사용자를 찾을 수 없습니다."),
  ALREADY_EXIST_MEMBER("이미 존재하는 사용자 입니다."),
  ALREADY_REGISTERED_EMAIL("이미 등록된 이메일 입니다."),
  ALREADY_EXIST_USERNAME("닉네임이 중복됩니다. 다른 닉네임으로 설정해주세요"),
  EMAIL_IS_REQUIRED("이메일 제공 항목에 동의해야 서비스를 이용할 수 있습니다."),
  PASSWORD_MUST_BE_BETWEEN_8_TO_20_CHARCTERS("비밀번호는 8자 이상 20자 이하여야 합니다."),
  PASSWORD_MUST_HAVE_SPECIAL_CHARACTER("비밀번호에는 1개 이상의 특수문자가 들어가야 합니다."),
  ACCESS_TOKEN_NOT_FOUND("유효하지 않은 로그인입니다. 재로그인 해주세요."),
  REFRESH_TOKEN_EXPIRED("로그인 유효기간이 지났습니다. 재로그인 해주세요."),
  NOT_CORRECT_PASSWORD("비밀번호가 일치하지 않습니다."),
  ALREADY_REGISTERED_NICKNAME("이미 등록된 닉네임 입니다. 다른 닉네임을 사용해 주세요"),
  INVALID_REFRESH_TOKEN("refresh token이 유효하지 않습니다."),
  NOT_FOUND_TOKEN_SET("해당 accessToken으로 저장된 token을 찾을 수 없습니다."),
  UNREGISTERED_ACCOUNT("탈퇴한 회원입니다."),
  INVALID_KAKAO_CODE("카카오 로그인 결과가 유효하지 않습니다. 재시도 해주세요."),
  INVALID_KAKAO_TOKEN("카카오에서 잘못된 토큰값을 내려주었습니다. 재시도 해주세요.");

  private final String description;

}
