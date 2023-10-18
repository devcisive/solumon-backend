package com.example.solumonbackend.member.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoSignUpDto {

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Request {

    private String kakaoAccessToken;
    @NotBlank(message = "닉네임은 빈칸일 수 없습니다.")
    @Size(max = 10, message = "닉네임은 최대 10자입니다.")
    private String nickname;
  }

  @Getter
  @Builder
  public static class Response {

    private Long memberId;
    private Long kakaoId;
    private String email;
    private String nickname;
  }
}
