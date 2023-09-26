package com.example.solumonbackend.member.service;

import static com.example.solumonbackend.global.exception.ErrorCode.NOT_CORRECT_PASSWORD;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static com.example.solumonbackend.global.exception.ErrorCode.UNREGISTERED_MEMBER;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.CreateTokenDto;
import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.type.MemberRole;
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

  @Transactional
  public GeneralSignUpDto.Response signUp(GeneralSignUpDto.Request request) {
    System.out.println("validateDuplicatedEmail");
    validateDuplicatedEmail(request.getEmail());
    System.out.println("validateDuplicatedNickName");
    validateDuplicatedNickName(request.getNickname());
    System.out.println("checkPassword");

    return GeneralSignUpDto.Response.memberToResponse(memberRepository.save(Member.builder()
        .kakaoId(null)
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .role(MemberRole.GENERAL)
        .reportCount(0)
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

}
