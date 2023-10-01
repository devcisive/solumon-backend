package com.example.solumonbackend.member.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(timeToLive = 60 * 60 * 24 * 31 * 2)
@Getter
@Setter
public class RefreshToken {

  @Id
  private Long id;
  @Indexed
  private String accessToken;
  private String refreshToken;

  @Builder
  public RefreshToken(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
