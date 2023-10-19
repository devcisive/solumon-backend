package com.example.solumonbackend.global.config;

import com.example.solumonbackend.chat.model.ChatMemberInfo;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableRedisRepositories(basePackageClasses = RefreshTokenRedisRepository.class)
@Configuration
public class RedisConfig {

  @Value("${spring.redis.host}")
  private String redisHost;

  @Value("${spring.redis.port}")
  private int redisPort;

  // 0번 저장소 : 토큰
  // 1번 저장소 : 채팅멤버 정보

  // 레디스 데이터베이스 나누기용
  public RedisConnectionFactory createLettuceConnectionFactory(int dbIndex) {
    final RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(redisHost);
    redisStandaloneConfiguration.setPort(redisPort);
    redisStandaloneConfiguration.setDatabase(dbIndex);

    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }

  // 기존에 있던 토큰용 팩토리를 기본으로 설정
  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {
    return createLettuceConnectionFactory(0); // 0번 저장소
  }

  // 토큰 저장하는 저장소와 분리
  @Bean
  public RedisConnectionFactory redisChatMemberConnectionFactory() {
    return createLettuceConnectionFactory(1);
  }

  // 채팅할때 멤버 정보
  @Bean
  public RedisTemplate<String, ChatMemberInfo> redisChatMemberTemplate() {
    RedisTemplate<String, ChatMemberInfo> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisChatMemberConnectionFactory());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());

    return redisTemplate;
  }
}
