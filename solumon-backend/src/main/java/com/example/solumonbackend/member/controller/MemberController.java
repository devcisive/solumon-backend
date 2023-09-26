package com.example.solumonbackend.member.controller;

import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.model.KakaoSignInDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.member.model.StartWithKakao;
import com.example.solumonbackend.member.service.KakaoService;
import com.example.solumonbackend.member.service.MemberService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class MemberController {
  private final KakaoService kakaoService;
  private final MemberService memberService;

  @PostMapping("/sign-up/general")
  public ResponseEntity<GeneralSignUpDto.Response> signUp(@Valid @RequestBody GeneralSignUpDto.Request request) {
    log.info("[sign-up/general] 회원가입 진행. userEmail : {} ", request.getEmail());
    return ResponseEntity.ok(memberService.signUp(request));
  }

  @PostMapping("/sign-in/general")
  public ResponseEntity<GeneralSignInDto.Response> signIn(@Valid @RequestBody GeneralSignInDto.Request request) {
    return ResponseEntity.ok(memberService.signIn(request));
  }

  @GetMapping("/start/kakao")
  public ResponseEntity<StartWithKakao.Response> startWithKakao(@RequestParam(value = "code", required = false) String code) {
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

  @GetMapping("/exception")
  public void exception() throws RuntimeException {
    throw new RuntimeException("접근이 금지되었습니다.");
  }

  @GetMapping("/test")
  public void test(@AuthenticationPrincipal MemberDetail memberDetail) {
    System.out.println(memberDetail.getMember().getEmail());
  }
}
