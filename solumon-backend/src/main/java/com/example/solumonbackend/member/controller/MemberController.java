package com.example.solumonbackend.member.controller;

import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberLogDto;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.service.KakaoService;
import com.example.solumonbackend.member.service.MemberService;
import com.example.solumonbackend.post.model.MyParticipatePostDto;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostState;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

  @PostMapping("/sign-up/kakao")
  public ResponseEntity<?> kakaoSignUp(@RequestParam String code, @RequestParam String nickname) {
    return ResponseEntity.ok(kakaoService.kakaoSignUp(code, nickname));
  }

  @PostMapping("/sign-in/kakao")
  public ResponseEntity<?> kakaoSignIn(@RequestParam String code) {
    return ResponseEntity.ok(kakaoService.kakaoSignIn(code));
  }

  @PostMapping("/sign-in/general")
  public ResponseEntity<GeneralSignInDto.Response> signIn(@Valid @RequestBody GeneralSignInDto.Request request) {
    return ResponseEntity.ok(memberService.signIn(request));
  }

  @GetMapping("/exception")
  public void exception() throws RuntimeException {
    throw new RuntimeException("접근이 금지되었습니다.");
  }

  @GetMapping("/test")
  public void test(@AuthenticationPrincipal MemberDetail memberDetail) {
    System.out.println(memberDetail.getMember().getEmail());
  }


  @GetMapping
  public ResponseEntity<MemberLogDto.Info> getMyInfo(@AuthenticationPrincipal MemberDetail memberDetail) {

    return ResponseEntity.ok().body(memberService.getMyInfo(memberDetail.getMember()));
  }


  @GetMapping("/mylog")
  public ResponseEntity<Page<MyParticipatePostDto>> getMyParticipatePosts(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestParam(name = "postParticipateType") PostParticipateType postParticipateType,
      @RequestParam(name = "postState") PostState postState,
      @RequestParam(name = "postOrder") PostOrder postOrder,
      @RequestParam(name = "page", defaultValue = "1") int page
  ) {

    return ResponseEntity.ok()
        .body(memberService.getMyParticipatePosts(memberDetail.getMember(),
            postState, postParticipateType, postOrder,
            PageRequestCustom.of(page, postOrder)));
  }


  @PutMapping
  public ResponseEntity<MemberUpdateDto.Response> updateMyInfo(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestBody @Valid MemberUpdateDto.Request update) {
    return ResponseEntity.ok()
        .body(memberService.updateMyInfo(memberDetail.getMember(), update));
  }


  @DeleteMapping("/withdraw")
  public ResponseEntity<WithdrawDto.Response> withdrawMember(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestBody WithdrawDto.Request request) {
    return ResponseEntity.ok()
        .body(memberService.withdrawMember(memberDetail.getMember(), request));
  }


  @PostMapping("/interests")
  public ResponseEntity<MemberInterestDto.Response> registerInterest(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestBody MemberInterestDto.Request request) {
    return ResponseEntity.ok()
        .body(memberService.registerInterest(memberDetail.getMember(), request));
  }
}