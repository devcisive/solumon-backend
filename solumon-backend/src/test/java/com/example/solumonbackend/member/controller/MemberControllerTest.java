package com.example.solumonbackend.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.service.KakaoService;
import com.example.solumonbackend.member.service.MemberService;
import com.example.solumonbackend.member.type.MemberRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberService memberService;
  @Autowired
  private KakaoService kakaoService;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;


  @DisplayName("회원 가입 성공")
  @Test
  void signUp_Success() throws Exception {
    // given
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .nickname("testUser").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@naver.com"))
        .andExpect(jsonPath("$.nickname").value("testUser"));
  }

  @DisplayName("회원 가입 성공 - 닉네임 한글")
  @Test
  void signUp_Success_KoreanNickName() throws Exception {
    // given
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .nickname("테스트유저").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@naver.com"))
        .andExpect(jsonPath("$.nickname").value("테스트유저"));
  }

  @DisplayName("회원 가입 실패 - 중복된 이메일")
  @Test
  void signUpFail_DuplicatedEmail() throws Exception {
    // given
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .nickname("testUser").build();

    memberRepository.save(Member.builder()
        .kakaoId(null)
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname("testUser2")
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build());

    String json = objectMapper.writeValueAsString(request);

    // then
    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("ALREADY_REGISTERED_EMAIL"));
  }

  @DisplayName("회원 가입 실패 - 중복된 닉네임")
  @Test
  void signUpFail_DuplicatedNickName() throws Exception {
    // given
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .nickname("testUser").build();

    memberRepository.save(Member.builder()
        .kakaoId(null)
        .email("test2@naver.com")
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build());

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("ALREADY_REGISTERED_NICKNAME"));
  }

  @DisplayName("회원 가입 실패 - 사이즈가 너무 작은 닉네임")
  @Test
  void signUpFail_TooShortNickName() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .nickname("").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"));
    // 메세지가 여러개인 경우 메세지 순서가 바뀌어 실패가 뜰 수도 있어서 메세지 비교 안했습니다.

  }

  @DisplayName("회원 가입 실패 - 사이즈가 너무 큰 닉네임")
  @Test
  void signUpFail_TooLongNickName() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .nickname("SDFfwefdsgaggergdfgewfsdfWEF").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"))
        .andExpect(jsonPath("$.errorMessage").value("닉네임은 특수문자를 제외한 2~10자리여야 합니다. "));
  }

  @DisplayName("회원 가입 실패 - 올바르지 않은 닉네임. 특수문자 포함")
  @Test
  void signUpFail_WithSpecialCharacters() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .nickname("testUser!@#$").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"))
        .andExpect(jsonPath("$.errorMessage").value("닉네임은 특수문자를 제외한 2~10자리여야 합니다. "));
  }

  @DisplayName("회원 가입 실패 - 이메일 형식이 맞지 않음")
  @Test
  void signUpFail_InvalidEmailFormat() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("testUser")
        .password("password123!@#")
        .nickname("testUser").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"))
        .andExpect(jsonPath("$.errorMessage").value("이메일 형식이 올바르지 않습니다. "));
  }

  @DisplayName("회원 가입 실패 - 비밀번호 공백")
  @Test
  void signUpFail_BlankAndTooShortPassword() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("testUser@naver.com")
        .password("    ")
        .nickname("testUser").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"));
  }

  @DisplayName("회원 가입 실패 - 사이즈가 너무 큰 비밀번호")
  @Test
  void signUpFail_TooLongPassword() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("testUser@naver.com")
        .password("12345678toolongpassword!@#$")
        .nickname("testUser").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"))
        .andExpect(jsonPath("$.errorMessage").value("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 사용해야 합니다. "));
  }

  @DisplayName("회원 가입 실패 - 비밀번호 특수문자 미포함")
  @Test
  void signUpFail_NoSpecialCharacterInPassword() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("testUser@naver.com")
        .password("password1234")
        .nickname("testUser").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"))
        .andExpect(jsonPath("$.errorMessage").value("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 사용해야 합니다. "));
  }

  @DisplayName("회원 가입 실패 - 비밀번호 숫자 미포함")
  @Test
  void signUpFail_NoNumberInPassword() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("testUser@naver.com")
        .password("password!@#$")
        .nickname("testUser").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"))
        .andExpect(jsonPath("$.errorMessage").value("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 사용해야 합니다. "));
  }

  @DisplayName("회원 가입 실패 - 비밀번호 영어 미포함")
  @Test
  void signUpFail_NoAlphabetInPassword() throws Exception {
    GeneralSignUpDto.Request request = GeneralSignUpDto.Request.builder()
        .email("testUser@naver.com")
        .password("12345678!@#$")
        .nickname("testUser").build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/sign-up/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("MethodArgumentNotValidException"))
        .andExpect(jsonPath("$.errorMessage").value("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 사용해야 합니다. "));
  }

  @DisplayName("로그인 성공")
  @Test
  void signIn_Success() throws Exception {
    // given
    GeneralSignInDto.Request request = GeneralSignInDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .build();

    memberRepository.save(Member.builder()
        .memberId(5L)
        .kakaoId(null)
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname("testUser")
        .role(MemberRole.GENERAL)
//        .reportCount(0)
        .isFirstLogIn(true)
        .build());

    String json = objectMapper.writeValueAsString(request);

    // then
    mockMvc.perform(post("/user/sign-in/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").isString())
        .andExpect(jsonPath("$.refresh_token").isString())
        .andExpect(jsonPath("$.is_first_login").value(true))
        .andExpect(jsonPath("$.access_token").isNotEmpty())
        .andExpect(jsonPath("$.refresh_token").isNotEmpty());
  }

  @DisplayName("로그인 실패 - 입력된 이메일과 일치하는 유저 없음")
  @Test
  void signInFail_NotFoundMember() throws Exception {
    // given
    GeneralSignInDto.Request request = GeneralSignInDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .build();

    String json = objectMapper.writeValueAsString(request);

    // then
    mockMvc.perform(post("/user/sign-in/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("NOT_FOUND_MEMBER"));
  }

  @DisplayName("로그인 실패 - 비밀번호 다름")
  @Test
  void signInFail_NotCorrectPassword() throws Exception {
    // given
    GeneralSignInDto.Request request = GeneralSignInDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .build();

    memberRepository.save(Member.builder()
        .memberId(5L)
        .kakaoId(null)
        .email(request.getEmail())
        .password(passwordEncoder.encode("otherPassword12!@"))
        .nickname("testUser")
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build());

    String json = objectMapper.writeValueAsString(request);

    // then
    mockMvc.perform(post("/user/sign-in/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("NOT_CORRECT_PASSWORD"));
  }

  @DisplayName("로그인 실패 - 이미 탈퇴한 회원")
  @Test
  void signInFail_UnregisteredMember() throws Exception {
    // given
    GeneralSignInDto.Request request = GeneralSignInDto.Request.builder()
        .email("test@naver.com")
        .password("password123!@#")
        .build();

    memberRepository.save(Member.builder()
        .memberId(5L)
        .kakaoId(null)
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname("testUser")
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .unregisteredAt(LocalDateTime.now())
        .build());

    String json = objectMapper.writeValueAsString(request);

    // then
    mockMvc.perform(post("/user/sign-in/general")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Failed"))
        .andExpect(jsonPath("$.errorCode").value("UNREGISTERED_MEMBER"));
  }


}