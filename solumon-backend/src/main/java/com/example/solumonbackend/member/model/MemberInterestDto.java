package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MemberInterestDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    @NotNull
    private List<String> interests;

  }


  @Builder
  @Getter
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