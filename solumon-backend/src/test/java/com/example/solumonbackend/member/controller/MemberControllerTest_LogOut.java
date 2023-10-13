package com.example.solumonbackend.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.service.MemberService;
import com.example.solumonbackend.member.type.MemberRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest_LogOut {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberService memberService;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private RefreshTokenRedisRepository refreshTokenRedisRepository;

  @BeforeEach
  public void setUp() {
    memberRepository.save(Member.builder()
        .email("sample@sample.com")
        .role(MemberRole.GENERAL)
        .build());
  }

  @AfterEach
  public void cleanUp() {
    //redisRepository는 @transactional 적용을 받지 않아서 내버려둠
    refreshTokenRedisRepository.deleteAll();
  }

  @Test
  @WithUserDetails(value = "sample@sample.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void logOut_success() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));
    //when

    //then
    mockMvc.perform(get("/user/log-out")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("로그아웃 되었습니다."));

    Assertions.assertEquals("logout", refreshTokenRedisRepository.findByAccessToken("accessToken").get().getRefreshToken());
  }

  @Test
  @WithUserDetails(value = "sample@sample.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void logOut_fail_accessTokenNotFound() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(get("/user/log-out")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ACCESS_TOKEN_NOT_FOUND.toString()));
  }
}