package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MemberLogDto {

  @Builder
  @Getter
  @Setter
  public static class Info {

    private Long memberId;
    private String nickname;
    private String email;
    private List<String> interests;

    public static MemberLogDto.Info memberToResponse(Member member, List<String> interests) {
      return Info.builder()
          .memberId(member.getMemberId())
          .email(member.getEmail())
          .nickname(member.getNickname())
          .interests(interests)
          .build();
    }
  }

}
