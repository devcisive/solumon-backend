package com.example.solumonbackend.member.controller;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberLogDto;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.model.WithdrawDto.Response;
import com.example.solumonbackend.member.service.MemberService;
import com.example.solumonbackend.post.model.MyActivePostDto;
import com.example.solumonbackend.post.type.PostActiveType;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostState;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final MemberService memberService;

  @PostMapping("/sign-up/general")
  public ResponseEntity<GeneralSignUpDto.Response> signUp(
      @Valid @RequestBody GeneralSignUpDto.Request request) {
    log.info("[sign-up/general] 회원가입 진행. userEmail : {} ", request.getEmail());
    return ResponseEntity.ok(memberService.signUp(request));
  }

  @PostMapping("/sign-in/general")
  public ResponseEntity<GeneralSignInDto.Response> signIn(
      @Valid @RequestBody GeneralSignInDto.Request request) {
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
   * @param member
   * @return
   */
  @GetMapping
  public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal Member member) {

    MemberLogDto.Info response = memberService.getMyInfo(member);
    return ResponseEntity.ok().body(response);
  }


  /**
   * (#6) 내 활동한 게시글목록 조회 (작성한 글/ 채팅참여한 글/ 투표한 참여글)
   *
   * @param member
   * @param postActiveType
   * @param postState
   * @param postOrder
   * @return
   */
  @GetMapping("/users/mylog")
  public ResponseEntity<?> getMyActivePosts(@AuthenticationPrincipal Member member,
      @RequestParam PostActiveType postActiveType, @RequestParam PostState postState,
      @RequestParam PostOrder postOrder) {

    List<MyActivePostDto> myActivePosts = memberService.getMyActivePosts(member, postState,
        postActiveType, postOrder);

    return ResponseEntity.ok().body(myActivePosts);
  }

  /**
   * (#6) 내 정보 수정 (프로필)
   *
   * @param member
   * @return
   */
  @PutMapping
  public ResponseEntity<?> updateMyInfo(@AuthenticationPrincipal Member member,
      @RequestBody @Valid MemberUpdateDto.Request update) {

    MemberUpdateDto.Response response = memberService.updateMyInfo(member, update);
    return ResponseEntity.ok().body(response);
  }


  /**
   * (#6) 회원 탈퇴
   *
   * @param member
   * @param request
   * @return
   */
  @DeleteMapping("/withdraw")
  public ResponseEntity<?> withdrawMember(@AuthenticationPrincipal Member member,
      @RequestBody WithdrawDto.Request request) {

    Response response = memberService.withdrawMember(member, request);
    return ResponseEntity.ok().body(response);
  }


  /**
   * (#8) 관심주제 선택
   *
   * @param member
   * @param request
   * @return
   */
  @PostMapping("/interests")
  public ResponseEntity<?> registerInterest(@AuthenticationPrincipal Member member,
      @Valid @RequestBody MemberInterestDto.Request request) {

    MemberInterestDto.Response response = memberService.registerInterest(member, request);
    return ResponseEntity.ok().body(response);

  }
}
