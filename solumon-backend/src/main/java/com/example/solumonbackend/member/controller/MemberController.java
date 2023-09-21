package com.example.solumonbackend.member.controller;

import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.member.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")

public class MemberController {
  private final KakaoService kakaoService;

  @PostMapping("/sign-up/kakao")
  public ResponseEntity<?> kakaoSignUp(@RequestParam String code, @RequestParam String nickname) {
    return ResponseEntity.ok(kakaoService.kakaoSignUp(code, nickname));
  }

  @PostMapping("/sign-in/kakao")
  public ResponseEntity<?> kakaoLogIn(@RequestParam String code) {
    return ResponseEntity.ok(kakaoService.kakaoLogIn(code));
  }

  @PostMapping("/update-token/kakao")
  public ResponseEntity<?> kakaoTokenUpdate(@AuthenticationPrincipal MemberDetail memberDetail,
                                            @RequestHeader("X-AUTH-TOKEN") String oldAccessToken) {
    return ResponseEntity.ok(kakaoService.kakaoTokenUpdate(memberDetail, oldAccessToken));
  }

  @PostMapping("/log-out/kakao")
  public ResponseEntity<?> kakaoLogOut(@AuthenticationPrincipal MemberDetail memberDetail) {
    return ResponseEntity.ok(kakaoService.kakaoLogOut(memberDetail));
  }
}
