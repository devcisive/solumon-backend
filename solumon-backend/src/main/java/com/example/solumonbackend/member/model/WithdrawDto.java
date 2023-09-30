package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;



public class WithdrawDto {

  @Getter
  @AllArgsConstructor
  public static class Request {

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



