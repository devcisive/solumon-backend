package com.example.solumonbackend.member.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class KakaoUserInfoDto {

  @JsonProperty("id")
  private Long kakaoId;
  @JsonProperty("kakao_account")
  private KakaoAccount kakaoAccount;

  @Getter
  public static class KakaoAccount {

    @JsonProperty("email")
    private String email;
  }
}
