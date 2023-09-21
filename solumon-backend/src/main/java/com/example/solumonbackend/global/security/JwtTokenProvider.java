//package com.example.solumonbackend.global.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import java.util.Date;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtTokenProvider {
//
//  private final UserDetailsService memberDetailService;
//  @Value("${spring.jwt.secretKey}")
//  private String secretKey;
//  private static final long TOKEN_VALID_TIME = 1000L * 60 * 30;
//
//  public String createToken(String email, List<String> roles) {
//    Claims claims = Jwts.claims().setSubject(email);
//    claims.put("roles", roles);
//
//    Date now = new Date();
//    var expiredDate = new Date(now.getTime() + TOKEN_VALID_TIME);
//
//    return Jwts.builder()
//        .setClaims(claims)
//        .setIssuedAt(now)
//        .setExpiration(expiredDate)
//        .signWith(SignatureAlgorithm.HS256, this.secretKey)
//        .compact();
//  }
//
//  public Authentication getAuthentication(String token) {
//    log.info("[getAuthentication] 토큰 인증 정보 조회");
//    UserDetails userDetails =
//  }
//
//
//
//}
