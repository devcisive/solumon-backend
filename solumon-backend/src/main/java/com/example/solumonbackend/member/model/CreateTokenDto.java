package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.type.MemberRole;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateTokenDto {

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
