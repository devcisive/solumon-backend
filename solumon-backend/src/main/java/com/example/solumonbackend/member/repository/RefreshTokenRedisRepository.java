package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;

@EnableRedisRepositories
public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByAccessToken(String accessToken);

  void deleteByAccessToken(String accessToken);
}
