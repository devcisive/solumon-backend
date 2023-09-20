package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import lombok.Builder;
import lombok.Getter;


public class WithdrawDto {

  @Getter
  public static class Request {

    private String password;

  }


  @Builder
  public static class Response {

    private Long member_id;
    private String email;
    private String nickname;

    public static Response of(Member member) {
      return Response.builder()
          .member_id(member.getMemberId())
          .email(member.getEmail())
          .nickname(member.getNickname())
          .build();
    }
  }
}



