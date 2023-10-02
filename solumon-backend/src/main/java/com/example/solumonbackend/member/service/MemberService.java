package com.example.solumonbackend.member.service;

import static com.example.solumonbackend.global.exception.ErrorCode.ACCESS_TOKEN_NOT_FOUND;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_CORRECT_PASSWORD;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static com.example.solumonbackend.global.exception.ErrorCode.UNREGISTERED_MEMBER;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.entity.Report;
import com.example.solumonbackend.member.model.CreateTokenDto;
import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto.Response;
import com.example.solumonbackend.member.model.ReportDto;
import com.example.solumonbackend.member.model.LogOutDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.repository.ReportRepository;
import com.example.solumonbackend.member.type.MemberRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
  private final ReportRepository reportRepository;

  @Transactional
  public GeneralSignUpDto.Response signUp(GeneralSignUpDto.Request request) {
    validateDuplicatedEmail(request.getEmail());
    validateDuplicatedNickName(request.getNickname());

    return GeneralSignUpDto.Response.memberToResponse(memberRepository.save(Member.builder()
        .kakaoId(null)
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build()));
  }

  private void validateDuplicatedEmail(String email) {
    if (memberRepository.findByEmail(email).isPresent()) {
      throw new MemberException(ErrorCode.ALREADY_REGISTERED_EMAIL);
    }
  }

  private void validateDuplicatedNickName(String nickName) {
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
    // 탈퇴한 유저 걸러내기
    if (member.getUnregisteredAt() != null) {
      throw new MemberException(UNREGISTERED_MEMBER);
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
        .refreshToken(refreshToken)
        .isFirstLogIn(member.isFirstLogIn())
        .build();
  }

  @Transactional
  public LogOutDto.Response logOut(Member member, String accessToken) {
    RefreshToken refreshToken = refreshTokenRedisRepository.findByAccessToken(accessToken)
        .orElseThrow(() -> new MemberException(ACCESS_TOKEN_NOT_FOUND));

    // 해당 액서스 토큰과 연결된 리프레시 토큰을 삭제하고 그 자리에 로그아웃 기록
    refreshToken.setRefreshToken("logout");
    refreshTokenRedisRepository.save(refreshToken);

    return LogOutDto.Response.builder()
        .memberId(member.getMemberId())
        .status("로그아웃 되었습니다.")
        .build();
  }


  @Transactional
  public void reportMember(Member member, Long reportedMemberId, ReportDto.Request request) {

    // 피신고자가 존재하는 유저인지 확인
    Optional<Member> optionalMember = memberRepository.findByMemberId(reportedMemberId);
    if (optionalMember.isEmpty() || optionalMember.get().getUnregisteredAt() != null) {
      throw new MemberException(NOT_FOUND_MEMBER);
    }
    Member reportedMember = optionalMember.get();

    // 이미 정지상태면 신고불가
    if (reportedMember.getBannedAt() != null) {
      throw new MemberException(ErrorCode.ALREADY_BANNED_MEMBER);
    }

    // 내가 3일안에 신고한 유저인지 확인 (가장 최근의 신고날짜를 확인)
    Optional<Report> latestReportByMe
        = reportRepository.findTopByMemberAndReporterIdOrderByReportedAtDesc(reportedMember,
        member.getMemberId());

    if (latestReportByMe.isPresent()
        && latestReportByMe.get().getReportedAt().plusDays(3).isAfter(LocalDateTime.now())) {
      throw new MemberException(ErrorCode.COOL_TIME_REPORT_MEMBER);
    }

    // 신고가 가능한 상태라면 신고
    reportRepository.save(
        Report.builder()
            .member(reportedMember)
            .reporterId(member.getMemberId())
            .reportType(request.getReportType())
            .content(request.getReportContent())
            .reportedAt(LocalDateTime.now())
            .build()
    );

    // 정지조건 충족 시 정지상태로 변환
    banMember(reportedMember);
  }


  @Transactional
  public void banMember(Member member) {

    int reportedCount = reportRepository.countByMember_MemberId(member.getMemberId());

    if (reportedCount >= 15) {      // 영구정지(ROLE_PERMANENT_BAN)
      member.setRole(MemberRole.PERMANENT_BAN);
      member.setBannedAt(LocalDateTime.now());

    } else if (reportedCount % 5 == 0) {     // 정지(BANNED)
      member.setRole(MemberRole.BANNED);
      member.setBannedAt(LocalDateTime.now());
    }

    memberRepository.save(member);

  }


  @Transactional
  public void releaseBan() {

    // 정지상태를 해제할 조건이 되는 멤버들을 뽑아온다.
    List<Member> membersToReleaseBan = memberRepository.findMembersToReleaseBannedStatus();

    // 정지해제
    for (Member member : membersToReleaseBan) {
      member.setBannedAt(null);
      member.setRole(MemberRole.GENERAL);
    }
  }
}