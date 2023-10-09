package com.example.solumonbackend.global.security;

import com.example.solumonbackend.global.exception.CustomSecurityException;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    log.info("[doFilterInternal] resolve Token");
    String accessToken = jwtTokenProvider.resolveToken(request);

    // 제대로 됐을 때
    if (accessToken != null & jwtTokenProvider.validateTokenExpiration(accessToken)) {
      log.info("[doFilterInternal] 토큰 유효 검증 성공");

      RefreshToken byAccessToken = refreshTokenRedisRepository.findByAccessToken(accessToken)
          .orElseThrow(() -> new CustomSecurityException(ErrorCode.NOT_FOUND_TOKEN_SET));

      // 로그아웃 처리된 액서스 토큰이 아닌지 검증, 아닐 시에만 authentication 부여
      // 로그아웃 처리된 액서스 토큰일 경우 authentication 부여하지 않고 401 error
      if (!"logout".equals(byAccessToken.getRefreshToken())) {
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } else if (accessToken != null & !jwtTokenProvider.validateTokenExpiration(accessToken)) {
      // 액세스 토큰으로 레디스에서 리프레쉬 토큰 가져오기
      RefreshToken byAccessToken = refreshTokenRedisRepository.findByAccessToken(accessToken)
          .orElseThrow(() -> new CustomSecurityException(ErrorCode.NOT_FOUND_TOKEN_SET));

      // 1. 리프레쉬도 만료됐다면 -> 다시 로그인 하도록 함
      if (!jwtTokenProvider.validateTokenExpiration(byAccessToken.getRefreshToken())) {
        throw new CustomSecurityException(ErrorCode.INVALID_REFRESH_TOKEN);
      }

      // 얘네가 정상이라면? 다시 AccessToken만들어서 기존 RefreshToken이랑 저장한 후 accessToken만 가져오기
      accessToken = jwtTokenProvider.reIssue(byAccessToken.getAccessToken());

      response.setHeader("X-AUTH-TOKEN", accessToken);
      Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }
}
