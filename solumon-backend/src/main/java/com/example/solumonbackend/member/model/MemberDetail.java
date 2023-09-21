package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.ArrayList;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
public class MemberDetail implements UserDetails {

  private Member member;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> collection = new ArrayList<>();
    collection.add(new GrantedAuthority() {
      @Override
      public String getAuthority() {
        return member.getRole().value();
      }
    });

    return collection;
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @JsonProperty(access = Access.WRITE_ONLY)
  @Override
  public String getUsername() {
    return member.getEmail();
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
