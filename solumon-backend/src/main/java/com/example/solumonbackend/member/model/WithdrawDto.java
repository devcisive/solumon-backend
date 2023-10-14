package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import javax.validation.constraints.NotBlank;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WithdrawDto {

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    @NotBlank(message = "비밀번호는 필수입력값입니다.")
    private String password;

  }


  @Builder
  @Getter
  public static class Response {

    private Long memberId;
    private String email;
    private String nickname;

    public static Response memberToResponse(Member member) {
      return Response.builder()
          .memberId(member.getMemberId())
          .email(member.getEmail())
          .nickname(member.getNickname())
          .build();
    }
  }
}



