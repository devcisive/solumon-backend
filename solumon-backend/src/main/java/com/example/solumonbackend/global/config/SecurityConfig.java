package com.example.solumonbackend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  private final ObjectMapper objectMapper;

  @Bean
  protected SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

    httpSecurity.
        httpBasic().disable() // REST API 는 UI를 사용하지 않으므로 기본설정을 비활성화
        .csrf().disable()  // REST API 는 csrf 보안이 필요 없으므로 비활성화

        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and() // JWT Token 인증방식으로 세션은 필요 없으므로 비활성화

        // 회원가입, 로그인은 모두에게 허용
        .authorizeRequests()
        .antMatchers("/", "/user/sign-up/**", "/user/sign-in/**", "/exception")
        .permitAll()

        .antMatchers().authenticated() // 인증받은 사람이면 모두 가능
        .anyRequest().authenticated(); // 그 외의 요청들은 인증받은 사람이면 모두 가능

    return httpSecurity.build();
  }
}