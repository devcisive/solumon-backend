package com.example.solumonbackend.member.service;

import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberLogDto;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.MyActivePostDto;
import com.example.solumonbackend.post.type.PostActiveType;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostState;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;

  @Transactional
  public GeneralSignUpDto.Response signUp(GeneralSignUpDto.Request request) {
    log.info("[signUp] 이메일 중복 확인. email : {}", request.getEmail());
    validateDuplicatedEmail(request.getEmail());
    log.info("[signUp] 이메일 중복 확인 통과");
    log.info("[signUp] 닉네임 중복 확인. nickname : {}", request.getNickname());
    validateDuplicatedNickName(request.getNickname());
    log.info("[signUp] 닉네임 중복 확인 통과");

    return Response.memberToResponse(memberRepository.save(
        Member.builder().email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword())).nickname(request.getNickname())
            .role(MemberRole.GENERAL).reportCount(0).banCount(0).build()));
  }

  public void validateDuplicatedEmail(String email) {
    log.info("[MemberService : validateDuplicated]");
    if (memberRepository.findByEmail(email).isPresent()) {
      throw new MemberException(ErrorCode.ALREADY_REGISTERED_EMAIL);
    }
  }

  public void validateDuplicatedNickName(String nickName) {
    if (memberRepository.findByNickname(nickName).isPresent()) {
      throw new MemberException(ErrorCode.ALREADY_REGISTERED_NICKNAME);
    }
  }

  @Transactional
  public GeneralSignInDto.Response signIn(GeneralSignInDto.Request request) {
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(NOT_CORRECT_PASSWORD);
    }

    CreateTokenDto createTokenDto = CreateTokenDto.builder().memberId(member.getMemberId())
        .email(member.getEmail()).role(member.getRole()).build();

    String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(),
        createTokenDto.getRoles());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(),
        createTokenDto.getRoles());

    refreshTokenRedisRepository.save(new RefreshToken(accessToken, refreshToken));

    return GeneralSignInDto.Response.builder().memberId(member.getMemberId())
        .accessToken(accessToken).refreshToken(refreshToken).build();
  }

  /**
   * (#6) 내 정보 조회
   *
   * @param member
   * @return
   */
  public MemberLogDto.Info getMyInfo(Member member) {

    List<MemberTag> memberTags = memberTagRepository.findAllByMember(member);
    List<String> strMemberTags = this.toStrMemberTags(memberTags);

    return MemberLogDto.Info.of(member, strMemberTags);
  }


  /**
   * (#6) 내 활동한 게시글목록 조회 (작성한 글/ 채팅참여한 글/ 투표한 참여글)
   *
   * @param member
   * @return
   */
  public List<MyActivePostDto> getMyActivePosts(Member member, PostState postState,
      PostActiveType postActiveType, PostOrder postOrder) {

    return postRepository.getMyActivePosts(member.getMemberId(), postState, postActiveType,
        postOrder);

  }


  /**
   * (#6) 내 정보 수정하기
   *
   * @param member
   * @param request
   * @return
   */
  public MemberUpdateDto.Response updateMyInfo(Member member, MemberUpdateDto.Request request) {

    //기존 비밀번호가 일치해야 정보수정 가능
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(NOT_CORRECT_PASSWORD);
    }

    String newPassword1 = request.getNewPassword1();
    if (newPassword1 != null) {
      if (!newPassword1.equals(request.getNewPassword2())) {
        throw new MemberException(NOT_CORRECT_PASSWORD);
      }
      String newEncodingPassword = passwordEncoder.encode(request.getPassword());
      member.setPassword(newEncodingPassword);
    }

    member.setNickname(request.getNickname());
    memberRepository.save(member);

    List<MemberTag> memberTags = memberTagRepository.findAllByMember(member);
    List<String> strMemberTags = this.toStrMemberTags(memberTags);

    return MemberUpdateDto.Response.of(member, strMemberTags);

  }


  /**
   * #6 회원탈퇴
   *
   * @param member
   * @param request
   */
  public WithdrawDto.Response withdrawMember(Member member, WithdrawDto.Request request) {

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(NOT_CORRECT_PASSWORD);
    }

    member.setUnregisteredAt(LocalDateTime.now());
    memberRepository.save(member);

    return WithdrawDto.Response.of(member);
  }


  /**
   * (#8) 관심주제 설정
   *
   * @param member
   * @param request
   * @return
   */
  public MemberInterestDto.Response registerInterest(Member member,
      MemberInterestDto.Request request) {

    memberTagRepository.deleteAllByMember(member);

    // 컨트롤러에서 String 으로 받아온 태그이름을 통해 tag 엔티티를 꺼내서 MemberTag 로 저장
    List<Tag> tags = new ArrayList<>();
    for (String interest : request.getInterests()) {
      tags.add(tagRepository.findByName(interest)
          .orElseThrow(() -> new RuntimeException(NOT_FOUND_TAG.getDescription()))); //임시로 해놓은 상태
    }

    for (Tag tag : tags) {
      memberTagRepository.save(new MemberTag(member, tag));
    }

    List<MemberTag> memberTags = memberTagRepository.findAllByMember(member);

    List<String> strMemberTags = this.toStrMemberTags(memberTags);
    return MemberInterestDto.Response.of(member, strMemberTags);
  }

  // 태그용 메소드(임시)
  public List<String> toStrMemberTags(List<MemberTag> memberTags) {
    List<String> interests = new ArrayList<>();

    for (MemberTag memberTag : memberTags) {
      interests.add(String.valueOf(memberTag.getTag()));
    }

    return interests;
  }
}
