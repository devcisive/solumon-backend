package com.example.solumonbackend.member.controller;

import com.example.solumonbackend.global.mail.EmailAuthResponseDto;
import com.example.solumonbackend.global.mail.EmailAuthService;
import com.example.solumonbackend.member.model.*;
import com.example.solumonbackend.member.service.MemberService;
import com.example.solumonbackend.post.model.MyParticipatePostDto;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class MemberController {

  private final MemberService memberService;
  private final EmailAuthService emailAuthService;

  @PostMapping("/sign-up/general")
  public ResponseEntity<GeneralSignUpDto.Response> signUp(
      @Valid @RequestBody GeneralSignUpDto.Request request) {
    log.info("[sign-up/general] 회원가입 진행. userEmail : {} ", request.getEmail());
    return ResponseEntity.ok(memberService.signUp(request));
  }

  @GetMapping(value = "/send-email-auth", produces = "application/json")
  @ResponseBody
  public ResponseEntity<EmailAuthResponseDto> sendEmailAuth(@RequestParam String email)
      throws Exception {
    String code = emailAuthService.sendSimpleMessage(email);
    log.debug("[sendEmailAuth] 인증코드 발송완료");
    log.debug("받는 이메일 : {}", email);
    log.debug("받는 코드 : {}", code);

    return ResponseEntity.ok(EmailAuthResponseDto.builder()
        .email(email)
        .code(code)
        .build());
  }

  @PostMapping("/sign-in/general")
  public ResponseEntity<GeneralSignInDto.Response> signIn(
      @Valid @RequestBody GeneralSignInDto.Request request) {
    return ResponseEntity.ok(memberService.signIn(request));
  }

  @GetMapping("/log-out")
  public ResponseEntity<LogOutDto.Response> logOut(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestHeader("X-AUTH-TOKEN") String accessToken) {
    return ResponseEntity.ok(memberService.logOut(memberDetail.getMember(), accessToken));
  }

  @GetMapping(value = "/find-password")
  public ResponseEntity<String> findPassword(@RequestBody FindPasswordDto.Request request)
      throws Exception {
    emailAuthService.sendTempPasswordMessage(request.getEmail());
    log.debug("[sendEmailAuth] 임시 비밀번호 발송완료");

    return ResponseEntity.ok(request.getEmail());
  }

  @GetMapping
  public ResponseEntity<MemberLogDto.Info> getMyInfo(
      @AuthenticationPrincipal MemberDetail memberDetail) {

    return ResponseEntity.ok().body(memberService.getMyInfo(memberDetail.getMember()));
  }

  @GetMapping("/mylog")
  public ResponseEntity<Page<MyParticipatePostDto>> getMyParticipatePosts(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestParam(name = "postParticipateType") PostParticipateType postParticipateType,
      @RequestParam(name = "postStatus") PostStatus postStatus,
      @RequestParam(name = "postOrder") PostOrder postOrder,
      @RequestParam(name = "pageNum", defaultValue = "1") int pageNum
  ) {

    return ResponseEntity.ok()
        .body(memberService.getMyParticipatePosts(memberDetail.getMember(),
            postStatus, postParticipateType, postOrder,
            PageRequestCustom.of(pageNum, postOrder)));
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

  @PostMapping("/report")
  public ResponseEntity<Void> reportMember(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestBody ReportDto.Request reportRequest,
      @RequestParam(name = "nickname") String nickname) {

    memberService.reportMember(memberDetail.getMember(), nickname, reportRequest);
    return ResponseEntity.ok().build();

  }

  @PostMapping("/interests")
  public ResponseEntity<MemberInterestDto.Response> registerInterest(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestBody MemberInterestDto.Request request) {
    return ResponseEntity.ok()
        .body(memberService.registerInterest(memberDetail.getMember(), request));
  }
}