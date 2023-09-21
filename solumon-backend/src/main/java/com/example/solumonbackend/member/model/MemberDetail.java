package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.type.MemberRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class MemberDetail implements UserDetails {

  private Long memberId;
  private String email;
  private String password;
  private MemberRole role;
  private List<String> roles;

  @Builder
  public MemberDetail(Long memberId, String email, String password, MemberRole role) {
    this.memberId = memberId;
    this.email = email;
    this.password = password;
    this.role = role;
    this.roles = new ArrayList<>();
    roles.add(role.value());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.roles
        .stream().map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  @Override
  public String getPassword() {
    return password;
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  @Override
  public String getUsername() {
    return email;
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  @Override
  public boolean isEnabled() {
    return true;
  }
}
