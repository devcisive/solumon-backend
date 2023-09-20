package com.example.solumonbackend.global.config;

import com.example.solumonbackend.global.security.CustomAccessDeniedHandler;
import com.example.solumonbackend.global.security.CustomAuthenticationEntryPoint;
import com.example.solumonbackend.global.security.JwtAuthenticationFilter;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.httpBasic().disable()
        .csrf().disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        .and()
        .authorizeRequests()
        // 권한설정은 나중에 다시 설정
        .antMatchers("/", "user/sign-up/general", "user/sign-in/**", "exception").permitAll()
//        .antMatchers("/").hasRole("ROLE_GENERAL")
//        .antMatchers("/").hasRole("ROLE_BANNED")
//        .antMatchers("/").hasRole("ROLE_PERMANENT_BAN")
        .anyRequest().permitAll()

        .and()
        .exceptionHandling()
        .accessDeniedHandler(new CustomAccessDeniedHandler())
        .authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper))

        .and()
        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
            UsernamePasswordAuthenticationFilter.class);
  }
}
