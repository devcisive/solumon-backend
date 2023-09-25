package com.example.solumonbackend.member.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solumonbackend.global.config.SecurityConfig;
import com.example.solumonbackend.global.security.JwtAuthenticationFilter;
import com.example.solumonbackend.member.model.KakaoSignInDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
import com.example.solumonbackend.member.service.KakaoService;
import com.example.solumonbackend.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// excludeFilter로 security 설정들 해제
@WebMvcTest(value = MemberController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)}
)
class MemberControllerTest {
  @MockBean
  KakaoService kakaoService;
  @MockBean
  MemberService memberService;
  @MockBean // @EnableJpaAuditing 때문에 추가
  JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  public void setUp(WebApplicationContext webApplicationContext){
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext) // 원래는 standaloneSetup이 단위테스트에 더 적절하다고 하는데 globalexceptionhandler 쓰려고 선택
        .defaultRequest(post("/**").with(csrf())) //모든 경우에 대해 csrf 해제
        .build();
  }

  @Test
  @WithMockUser // security 설정 해제해서 기본적으로 붙여야 함 (아무것도 설정하지 않을 경우 모든 주소에 대해 인증 필요)
  void signUpKakao_success() throws Exception {
    //given
    given(kakaoService.kakaoSignUp(anyString(), anyString()))
        .willReturn(KakaoSignUpDto.Response.builder()
            .memberId(1L)
            .kakaoId(100L)
            .email("test@kakao.com")
            .nickname("kakao")
            .build());
    //when
    //then
    mockMvc.perform(post("/user/sign-up/kakao")
        .contentType(MediaType.APPLICATION_JSON)
        .queryParam("code", "")
        .queryParam("nickname", "kakao"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberId").value(1))
        .andExpect(jsonPath("$.kakaoId").value(100))
        .andExpect(jsonPath("$.email").value("test@kakao.com"))
        .andExpect(jsonPath("$.nickname").value("kakao"));
  }


  @Test
  @WithMockUser
  void signUpKakao_fail_nicknameBlank() throws Exception {
    //given
    given(kakaoService.kakaoSignUp(anyString(), anyString()))
        .willReturn(KakaoSignUpDto.Response.builder()
            .memberId(1L)
            .kakaoId(100L)
            .email("test@kakao.com")
            .nickname(" ")
            .build());
    //when
    //then
    mockMvc.perform(post("/user/sign-up/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("code", "")
            .queryParam("nickname", " "))
        .andDo(print())
        .andExpect((result) ->
            assertTrue(result.getResolvedException().getClass().isAssignableFrom(ConstraintViolationException.class)));
  }

  @Test
  @WithMockUser
  void signUpKakao_fail_nicknameTooLong() throws Exception {
    //given
    given(kakaoService.kakaoSignUp(anyString(), anyString()))
        .willReturn(KakaoSignUpDto.Response.builder()
            .memberId(1L)
            .kakaoId(100L)
            .email("test@kakao.com")
            .nickname("testkakaouser")
            .build());
    //when
    //then
    mockMvc.perform(post("/user/sign-up/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("code", "")
            .queryParam("nickname", "testkakaouser"))
        .andDo(print())
        .andExpect((result) ->
            assertTrue(result.getResolvedException().getClass().isAssignableFrom(ConstraintViolationException.class)));
  }

  @Test
  @WithMockUser
  void kakaoSignIn_success() throws Exception {
    //given
    given(kakaoService.kakaoSignIn(anyString()))
        .willReturn(KakaoSignInDto.Response.builder()
            .memberId(1L)
            .isFirstLogIn(true)
            .accessToken("accessToken")
            .refreshToken("refreshToken")
            .build());
    //when
    //then
    mockMvc.perform(post("/user/sign-in/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("code", ""))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberId").value(1))
        .andExpect(jsonPath("$.isFirstLogIn").value(true))
        .andExpect(jsonPath("$.accessToken").value("accessToken"))
        .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
  }
}