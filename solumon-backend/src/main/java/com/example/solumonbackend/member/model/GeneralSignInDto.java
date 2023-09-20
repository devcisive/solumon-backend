package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.type.MemberRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GeneralSignInDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
  }

  @Getter
  @Builder
  public static class Response {

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("access_token")
    private String accessToken;

//    @JsonProperty("refresh_token")
//    private String refreshToken;

  }

  @Getter
  public static class CreateTokenDto {

    private Long memberId;
    private String email;
    private List<String> roles;

    @Builder
    public CreateTokenDto(Long memberId, String email, MemberRole role) {
      this.memberId = memberId;
      this.email = email;
      this.roles = new ArrayList<>();
      roles.add(role.value());
    }
  }
}
