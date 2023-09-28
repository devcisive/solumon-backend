package com.example.solumonbackend.member.controller;

import com.example.solumonbackend.member.model.KakaoSignInDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
import com.example.solumonbackend.member.model.StartWithKakao.Response;
import com.example.solumonbackend.member.service.KakaoService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class KakaoController {
  private final KakaoService kakaoService;

  @GetMapping("/start/kakao")
  public ResponseEntity<Response> startWithKakao(@RequestParam(value = "code", required = false) String code) {
    return ResponseEntity.ok(kakaoService.startWithKakao(code));
  }

  @PostMapping("/sign-up/kakao")
  public ResponseEntity<KakaoSignUpDto.Response> kakaoSignUp(@Valid @RequestBody KakaoSignUpDto.Request request) {
    return ResponseEntity.ok(kakaoService.kakaoSignUp(request));
  }

  @PostMapping("/sign-in/kakao")
  public ResponseEntity<KakaoSignInDto.Response> kakaoSignIn(@Valid @RequestBody KakaoSignInDto.Request request) {
    return ResponseEntity.ok(kakaoService.kakaoSignIn(request));
  }
}
