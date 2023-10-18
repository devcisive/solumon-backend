package com.example.solumonbackend.member.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class KakaoTokenInfoDto {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("scope")
  private String scope;
}
