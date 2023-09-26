package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberUpdateDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.[0-9])(?=.[a-zA-Z])(?=.*[@#$%^&+=!]).{8,20}$", message = "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 사용해야 합니다.")
    private String password;

    @Pattern(regexp = "^(?=.[0-9])(?=.[a-zA-Z])(?=.*[@#$%^&+=!]).{8,20}$", message = "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 사용해야 합니다.")
    private String newPassword1;

    private String newPassword2;

  }


  @Builder
  @Getter
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
