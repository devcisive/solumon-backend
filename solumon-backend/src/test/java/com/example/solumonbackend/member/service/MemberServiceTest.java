package com.example.solumonbackend.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto.Response;
import com.example.solumonbackend.member.model.LogOutDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.type.MemberRole;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private RefreshTokenRedisRepository refreshTokenRedisRepository;

  @InjectMocks
  private MemberService memberService;

  @DisplayName("일반 회원가입 - 성공")
  @Test
  void signUpTest_Success() {
    // Given
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@example.com")
        .password("password123!")
        .nickname("testUser").build();

    Member mockMember = Member.builder()
        .memberId(5L)
        .kakaoId(null)
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build();

    when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(memberRepository.findByNickname("testUser")).thenReturn(Optional.empty());
    when(memberRepository.save(any(Member.class))).thenReturn(mockMember);

    // When
    Response response = memberService.signUp(request);

    // Then
    ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
    verify(memberRepository, times(1)).save(captor.capture());
    verify(passwordEncoder, times(2)).encode("password123!");

    assertThat(response.getEmail()).isEqualTo(request.getEmail());
    assertThat(response.getNickname()).isEqualTo(request.getNickname());
  }

  @DisplayName("일반 회원가입 실패 - 중복된 이메일")
  @Test
  void signUpTest_DuplicatedEmail() {
    // Given
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@example.com")
        .password("password123!")
        .nickname("testUser").build();

    when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new Member()));

    // when
    MemberException exception = assertThrows(MemberException.class,
        () -> memberService.signUp(request));

    // then
    verify(memberRepository, times(1)).findByEmail(request.getEmail());
    assertEquals(ErrorCode.ALREADY_REGISTERED_EMAIL, exception.getErrorCode());
  }

  @DisplayName("일반 회원가입 실패 - 중복된 닉네임")
  @Test
  void signUpTest_DuplicatedNickName() {
    // Given
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@example.com")
        .password("password123!")
        .nickname("testUser").build();

    when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(memberRepository.findByNickname("testUser")).thenReturn(Optional.of(new Member()));

    // when
    MemberException exception = assertThrows(MemberException.class,
        () -> memberService.signUp(request));

    // then
    verify(memberRepository, times(1)).findByEmail(request.getEmail());
    verify(memberRepository, times(1)).findByNickname(request.getNickname());
    assertEquals(ErrorCode.ALREADY_REGISTERED_NICKNAME, exception.getErrorCode());
  }

  @DisplayName("일반 로그인 - 성공")
  @Test
  void signInTest_Success() {
    // Given
    String email = "test@example.com";
    String password = "password123!";

    GeneralSignInDto.Request request = GeneralSignInDto.Request.builder()
        .email(email)
        .password(password)
        .build();

    Member mockMember = Member.builder()
        .memberId(5L)
        .kakaoId(null)
        .email(email)
        .password(passwordEncoder.encode(password))
        .nickname("testUser")
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build();

    when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));
    when(passwordEncoder.matches(password, mockMember.getPassword())).thenReturn(true);

    // When
    GeneralSignInDto.Response response = memberService.signIn(request);

    // Then
    verify(memberRepository, times(1)).findByEmail(email);
    verify(passwordEncoder, times(1)).matches(password, mockMember.getPassword());
    verify(jwtTokenProvider, times(1)).createAccessToken(email,
        Collections.singletonList("ROLE_GENERAL"));
    verify(jwtTokenProvider, times(1)).createRefreshToken(email,
        Collections.singletonList("ROLE_GENERAL"));
    verify(refreshTokenRedisRepository, times(1)).save(any(RefreshToken.class));

    assertNotNull(response);
    assertEquals(mockMember.getMemberId(), response.getMemberId());
    assertEquals(mockMember.isFirstLogIn(), response.isFirstLogIn());
  }

  @DisplayName("일반 로그인 실패 - 아이디 불일치")
  @Test
  void signInTest_IncorrectEmail() {
    // 저장 안되어있는 테스트 유저 정보
    String email = "test@example.com";
    String password = "password123!";

    GeneralSignInDto.Request request = GeneralSignInDto.Request.builder()
        .email(email)
        .password(password)
        .build();

    // when
    MemberException exception = assertThrows(MemberException.class,
        () -> memberService.signIn(request));

    // then
    verify(memberRepository, times(1)).findByEmail(email);

    assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
  }

  @DisplayName("일반 로그인 실패 - 비밀번호 불일치")
  @Test
  void signInTest_IncorrectPassWord() {
    // 저장 안되어있는 테스트 유저 정보
    String email = "test@example.com";
    String password = "password123!";

    GeneralSignInDto.Request request = GeneralSignInDto.Request.builder()
        .email(email)
        .password(password)
        .build();

    Member mockMember = Member.builder()
        .memberId(5L)
        .kakaoId(null)
        .email(email)
        .password(passwordEncoder.encode("password1456@!#$"))
        .nickname("testUser")
        .role(MemberRole.GENERAL)
//        .reportCount(0)
        .isFirstLogIn(true)
        .build();

    when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));

    // when
    MemberException exception = assertThrows(MemberException.class,
        () -> memberService.signIn(request));

    // then
    verify(memberRepository, times(1)).findByEmail(email);
    verify(passwordEncoder, times(1)).matches(password, mockMember.getPassword());

    assertEquals(ErrorCode.NOT_CORRECT_PASSWORD, exception.getErrorCode());
  }

  @DisplayName("일반 로그인 실패 - 탈퇴한 유저 로그인 시도")
  @Test
  void signInTest_LoginWithDeletedUse() {
    GeneralSignInDto.Request request = GeneralSignInDto.Request.builder()
        .email("test@example.com")
        .password("password123!")
        .build();

    Member mockMember = Member.builder()
        .memberId(5L)
        .kakaoId(null)
        .email("test@example.com")
        .password(request.getPassword())
        .nickname("testUser")
        .role(MemberRole.GENERAL)
//        .reportCount(0)
        .isFirstLogIn(true)
        .unregisteredAt(LocalDateTime.now())
        .build();

    when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockMember));
    when(passwordEncoder.matches(request.getPassword(), mockMember.getPassword())).thenReturn(true);

    // when
    MemberException exception = assertThrows(MemberException.class,
        () -> memberService.signIn(request));

    // then
    verify(memberRepository, times(1)).findByEmail(request.getEmail());
    verify(passwordEncoder, times(1)).matches(request.getPassword(), mockMember.getPassword());

    assertEquals(ErrorCode.UNREGISTERED_MEMBER, exception.getErrorCode());
  }


  @Test
  void logOut_success() {
    //given
    Member member = Member.builder()
        .email("sample@sample.com")
        .build();
    String accessToken = "accessToken";
    RefreshToken refreshToken = new RefreshToken("accessToken", "refreshToken");

    //when
    when(refreshTokenRedisRepository.findByAccessToken(accessToken))
        .thenReturn(Optional.of(refreshToken));

    LogOutDto.Response response = memberService.logOut(member, accessToken);
    //then
    verify(refreshTokenRedisRepository, times(1)).findByAccessToken(accessToken);
    Assertions.assertEquals(member.getMemberId(), response.getMemberId());
    Assertions.assertEquals("로그아웃 되었습니다.", response.getStatus());
  }

  @Test
  void logOut_fail_accessTokenNotFound() {
    //given
    Member member = Member.builder()
        .email("sample@sample.com")
        .build();
    String accessToken = "accessToken";

    //when
    when(refreshTokenRedisRepository.findByAccessToken(accessToken))
        .thenReturn(Optional.empty());

    MemberException exception = Assertions.assertThrows(MemberException.class, () -> memberService.logOut(member, accessToken));
    //then
    verify(refreshTokenRedisRepository, times(1)).findByAccessToken(accessToken);
    Assertions.assertEquals(ErrorCode.ACCESS_TOKEN_NOT_FOUND, exception.getErrorCode());
  }
}