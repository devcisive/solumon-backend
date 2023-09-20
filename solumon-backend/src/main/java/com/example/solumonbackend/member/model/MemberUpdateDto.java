package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberUpdateDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {
    // 정규식은 임시로 해놓은 상태
    @NotBlank(message = "닉네임은 필수항목입니다.")
    @Size(max = 10, message = "닉네임은 10자 이하여야합니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수항목입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,15}", message = "비밀번호는 8~15자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,15}", message = "비밀번호는 8~15자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String newPassword1;

    private String newPassword2;

  }


  @Builder
  public static class Response {

    private Long member_id;
    private String nickname;
    private List<String> interests;

    public static Response of(Member member, List<String> interests) {
      return Response.builder()
          .member_id(member.getMemberId())
          .nickname(member.getNickname())
          .interests(interests)
          .build();
    }

  }

}
