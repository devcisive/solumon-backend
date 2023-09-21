package com.example.solumonbackend.member.service;

import static com.example.solumonbackend.global.exception.ErrorCode.NOT_CORRECT_PASSWORD;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_FOUND_MEMBER;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Ban;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.CreateTokenDto;
import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto.Response;
import com.example.solumonbackend.member.repository.BanRepository;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.type.MemberRole;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;
  private final BanRepository banRepository;

  @Transactional
  public GeneralSignUpDto.Response signUp(GeneralSignUpDto.Request request) {
    log.info("[signUp] 이메일 중복 확인. email : {}", request.getEmail());
    validateDuplicatedEmail(request.getEmail());
    log.info("[signUp] 이메일 중복 확인 통과");
    log.info("[signUp] 닉네임 중복 확인. nickname : {}", request.getNickname());
    validateDuplicatedNickName(request.getNickname());
    log.info("[signUp] 닉네임 중복 확인 통과");

    return Response.memberToResponse(
        memberRepository.save(Member.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .role(MemberRole.GENERAL)
            .reportCount(0)
            .banCount(0)
            .build())
    );
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

    CreateTokenDto createTokenDto = CreateTokenDto.builder()
        .memberId(member.getMemberId())
        .email(member.getEmail())
        .role(member.getRole())
        .build();

    String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(),
        createTokenDto.getRoles());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(),
        createTokenDto.getRoles());

    refreshTokenRedisRepository.save(new RefreshToken(accessToken, refreshToken));

    return GeneralSignInDto.Response.builder()
        .memberId(member.getMemberId())
        .accessToken(accessToken)
        .refreshToken(
            refreshToken)
        .build();
  }

  /**
   * (#7) 유저 신고 (5회 이상시 자동 밴)
   *
   * @param member
   * @param memberId
   */
  public void reportMember(Member member, Long memberId) {

    // 피신고자가 존재하는 유저인지 확인
    Member reportedMember = memberRepository.findByMemberId(memberId)
        .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

    // 이미 신고당한 상태면 중복신고불가
    if (banRepository.existsByMember(reportedMember)) {
      throw new MemberException(ErrorCode.ALREADY_REPORT_MEMBER);
    }

    // 신고횟수 + 1
    reportedMember.setReportCount(member.getReportCount() + 1);

    // 신고횟수가 5의 배수일때마다 밴
    if (reportedMember.getBanCount() % 5 == 0) {
      reportedMember.setBanCount(member.getBanCount() + 1);
      reportedMember.setRole(MemberRole.BANNED);
    }

    // 밴이 3회 이상일 경우 영구정지
    if (reportedMember.getBanCount() >= 3) {
      reportedMember.setRole(MemberRole.PERMANENT_BAN);
    }

    memberRepository.save(reportedMember);

    banRepository.save(
        Ban.builder()
            .member(reportedMember)
            .bannedBy(member.getMemberId())
            .bannedAt(LocalDateTime.now())
            .build());

  }

}
