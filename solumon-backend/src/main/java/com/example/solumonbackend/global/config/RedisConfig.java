package com.example.solumonbackend.global.config;

import com.example.solumonbackend.chat.model.ChatMemberInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.redis.host}")
  private String redisHost;
  @Value("${spring.redis.port}")
  private int redisPort;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(redisHost, redisPort);
  }



  /* RedisTemplate:
  - 다양한 데이터 유형의 저장과 검색 가능\
  - 직렬화 역직렬화 기능을 제공하여 java 객체 <-> Redis 객체
  - 캐싱 매커니즘 구현에 사용 가능
  * */


  // 채팅할때 멤버 정보
  @Bean
  public RedisTemplate<String, ChatMemberInfo> redisChatMemberTemplate() {
    RedisTemplate<String, ChatMemberInfo> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());

    return redisTemplate;
  }

}
