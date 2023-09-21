package com.example.solumonbackend.member.service;

import static com.example.solumonbackend.global.exception.ErrorCode.NOT_CORRECT_PASSWORD;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_FOUND_MEMBER;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignInDto.CreateTokenDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto.Response;
import com.example.solumonbackend.member.repository.MemberRepository;
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
      throw new MemberException(ErrorCode.ALREADY_REGISTERED_EMAIL);
    }
  }

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
    return GeneralSignInDto.Response.builder()
        .memberId(member.getMemberId())
        .accessToken(jwtTokenProvider.createToken(member.getEmail(), createTokenDto.getRoles()))
        .build();
  }

  /**
   * 수정중
  @Transactional
  public TokenDto.Response reIssue(TokenDto.Request request) {
    if (!jwtTokenProvider.validateTokenExpiration(request.getRefreshToken())) {
      throw new CustomSecurityException(INVALID_REFRESH_TOKEN);
    }

    Member member = findMemberByToken(request);

    if (!member.getRefreshToken().equals(request.getRefreshToken())) {
      throw new InvalidRefreshTokenException();
    }

    String accessToken = jwtTokenProvider.createToken(member.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken();
    member.updateRefreshToken(refreshToken);
    return new TokenResponseDto(accessToken, refreshToken);
  }

  public Member findMemberByToken(TokenRequestDto requestDto) {
    Authentication auth = jwtTokenProvider.getAuthentication(requestDto.getAccessToken());
    UserDetails userDetails = (UserDetails) auth.getPrincipal();
    String username = userDetails.getUsername();
    return memberRepository.findByEmail(username).orElseThrow(MemberNotFoundException::new);
  }
  **/
}
