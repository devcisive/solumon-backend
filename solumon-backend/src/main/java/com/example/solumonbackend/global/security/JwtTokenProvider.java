package com.example.solumonbackend.global.security;

import static com.example.solumonbackend.global.exception.ErrorCode.ACCESS_TOKEN_NOT_FOUND;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.CreateTokenDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private static final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 60; // 1시간 밀리초
  private static final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 24 * 60 * 60; // 2개월 밀리초

  private final UserDetailsService memberDetailService;
  private final MemberRepository memberRepository;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;

  @Value("${spring.jwt.secretKey}")
  private String secretKey;

  @PostConstruct
  protected void init() {
    secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(String email, List<String> roles) {
    log.info("[createAccessToken]");
    return this.createToken(email, roles, ACCESS_TOKEN_VALID_TIME);
  }

  public String createRefreshToken(String email, List<String> roles) {
    log.info("[createRefreshToken]");
    return this.createToken(email, roles, REFRESH_TOKEN_VALID_TIME);
  }

  private String createToken(String email, List<String> roles, long tokenValidTime) {
    log.info("[createToken]");
    Claims claims = Jwts.claims().setSubject(email);
    claims.put("roles", roles);

    Date now = new Date();

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
        .signWith(SignatureAlgorithm.HS256, this.secretKey)
        .compact();
  }

  // 토큰으로 인증 객체(Authentication) 얻기
  public Authentication getAuthentication(String token) {
    log.info("[getAuthentication] 토큰 인증 정보 조회");
    UserDetails userDetails = memberDetailService.loadUserByUsername(getMemberEmail(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getMemberEmail(String token) {
    try {
      log.info("[getMemberEmail] token 에서 이메일 정보 추출");
      return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    } catch (ExpiredJwtException e) {
      return e.getClaims().getSubject();
    }
  }

  public boolean validateTokenExpiration(String token) {
    try {
      log.info("[validateTokenExpiration] 토큰 유효 기간 확인");
      Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
      return !claims.getBody().getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  public String resolveToken(HttpServletRequest request) {
    return request.getHeader("X-AUTH-TOKEN");
  }

  public String reIssue(String accessToken) {
    RefreshToken refreshToken = refreshTokenRedisRepository.findByAccessToken(accessToken)
        .orElseThrow(() -> new MemberException(ACCESS_TOKEN_NOT_FOUND));

    Member byEmail = memberRepository.findByEmail(getMemberEmail(refreshToken.getAccessToken()))
        .orElseThrow(() -> new MemberException(
            ErrorCode.NOT_FOUND_MEMBER));

    CreateTokenDto createTokenDto = CreateTokenDto.builder()
        .memberId(byEmail.getMemberId())
        .email(byEmail.getEmail())
        .role(byEmail.getRole()).build();

    // 원래 그냥 새로운 refreshToken객체를 만들어서 refreshToken만 같게 하고
    // 저장하고 말았는데 update로 accessToken 바꿔서 저장
    // -> 안그러면 같은 accessToken으로 refreshToken만료될 때까지 계속 사용가능
    String newAccessToken = createAccessToken(createTokenDto.getEmail(), createTokenDto.getRoles());

    refreshToken.setAccessToken(newAccessToken);
    refreshTokenRedisRepository.save(refreshToken);

    return accessToken;
  }

}
