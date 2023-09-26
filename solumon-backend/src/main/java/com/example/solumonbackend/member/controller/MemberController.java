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

  /**
   * (#6) 내 정보 조회(프로필)
   *
   * @param memberDetail
   * @return
   */
  @GetMapping
  public ResponseEntity<MemberLogDto.Info> getMyInfo(@AuthenticationPrincipal MemberDetail memberDetail) {

    MemberLogDto.Info response = memberService.getMyInfo(memberDetail.getMember());
    return ResponseEntity.ok().body(response);
  }


  /**
   * (#6) 내 활동한 게시글목록 조회 (작성한 글/ 채팅참여한 글/ 투표한 참여글)
   * @param memberDetail
   * @param postParticipateType
   * @param postState
   * @param postOrder
   * @param page
   * @return
   */
  @GetMapping("/mylog")
  public ResponseEntity<Page<MyParticipatePostDto>> getMyActivePosts(
      @AuthenticationPrincipal MemberDetail memberDetail,
      @RequestParam("postParticipateType") PostParticipateType postParticipateType,
      @RequestParam("postState") PostState postState,
      @RequestParam("postOrder") PostOrder postOrder,
      @RequestParam("page") int page
      ) {

    Page<MyParticipatePostDto> myParticipatePosts
        = memberService.getMyParticipatePosts(memberDetail.getMember(),
                        postState, postParticipateType, postOrder,
                        PageRequestCustom.of(page, postOrder));

    return ResponseEntity.ok().body(myParticipatePosts);
  }


  /**
   * (#6) 내 정보 수정 (프로필)
   *
   * @param memberDetail
   * @return
   */
  @PutMapping
  public ResponseEntity<MemberUpdateDto.Response> updateMyInfo(@AuthenticationPrincipal MemberDetail memberDetail,
      @RequestBody @Valid MemberUpdateDto.Request update) {

    MemberUpdateDto.Response response = memberService.updateMyInfo(memberDetail.getMember(), update);
    return ResponseEntity.ok().body(response);
  }


  /**
   * (#6) 회원 탈퇴
   *
   * @param memberDetail
   * @param request
   * @return
   */
  @DeleteMapping("/withdraw")
  public ResponseEntity<WithdrawDto.Response> withdrawMember(@AuthenticationPrincipal MemberDetail memberDetail,
      @RequestBody WithdrawDto.Request request) {

    WithdrawDto.Response response = memberService.withdrawMember(memberDetail.getMember(), request);
    return ResponseEntity.ok().body(response);
  }


  /**
   * (#8) 관심주제 선택
   *
   * @param memberDetail
   * @param request
   * @return
   */
  @PostMapping("/interests")
  public ResponseEntity<MemberInterestDto.Response> registerInterest(@AuthenticationPrincipal MemberDetail memberDetail,
      @RequestBody MemberInterestDto.Request request) {

    MemberInterestDto.Response response = memberService.registerInterest(memberDetail.getMember(), request);
    return ResponseEntity.ok().body(response);

  }
}
