package com.example.solumonbackend.member.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(timeToLive = 60 * 60 * 24 * 31 * 2)
@Getter
@Builder
public class RefreshToken {
  @Id
  private Long id;
  @Indexed
  private String accessToken;
  private String refreshToken;
}
