package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class MemberInterestDto {


  @Builder
  @Getter
  public static class Request {


    private Long memberId;
    private List<String> interests;


  }


  @Builder
  public static class Response {


    private Long memberId;
    private List<String> interests;

    public static Response memberToResponse(Member member, List<String> interests) {
      return Response.builder()
          .memberId(member.getMemberId())
          .interests(interests)
          .build();
    }

  }

}