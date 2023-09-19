package com.example.solumonbackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {

  NOT_FOUND_MEMBER("해당 사용자를 찾을 수 없습니다."),
  ALREADY_EXIST_MEMBER("이미 존재하는 사용자 입니다."),
  ALREADY_REGISTERED_EMAIL("이미 등록된 이메일 입니다."),
  ALREADY_EXIST_USERNAME("닉네임이 중복됩니다. 다른 닉네임으로 설정해주세요");

  private final String description;

}
