package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class MemberInterestDto {


  @Builder
  @Getter
  public static class Request {


    private Long member_id;
    private List<String> interests;


  }


  @Builder
  public static class Response {


    private Long member_id;
    private List<String> interests;

    public static Response of(Member member, List<String> interests) {
      return Response.builder()
          .member_id(member.getMemberId())
          .interests(interests)
          .build();
    }

  }

}