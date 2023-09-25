package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberUpdateDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    @NotBlank(message = "닉네임은 필수항목입니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 ~ 20자 입니다.")
    private String password;

    private String newPassword1;

    private String newPassword2;

  }


  @Builder
  public static class Response {

    private Long memberId;
    private String nickname;
    private List<String> interests;

    public static Response memberToResponse(Member member, List<String> interests) {
      return Response.builder()
          .memberId(member.getMemberId())
          .nickname(member.getNickname())
          .build();
    }

  }

}
