package com.example.solumonbackend.member.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.repository.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
@WithUserDetails(value = "fakeMember@naver.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class MemberControllerTest2 {


  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private MemberTagRepository memberTagRepository;



  @Autowired
  private PasswordEncoder passwordEncoder;



  private Member fakeMember;
  private Member otherMember;
  private Tag fakeTag;
  private MemberTag fakeMemberTag;



  @BeforeEach
  public void dataSetup() {

    // 기준이 될 멤버
    fakeMember = Member.builder()
        .memberId(1L)
        .kakaoId(1L)
        .email("fakeMember@naver.com")
        .nickname("닉네임")
        .registeredAt(LocalDateTime.now())
        .role(MemberRole.GENERAL)
        .modifiedAt(null)
        .unregisteredAt(null)
        .password(passwordEncoder.encode("password"))
        .isFirstLogIn(true)
        .build();
    memberRepository.save(fakeMember);



    otherMember = Member.builder()
        .memberId(2L)
        .kakaoId(2L)
        .email("fakeMember2@naver.com")
        .nickname("새로운닉네임")
        .registeredAt(LocalDateTime.now())
        .role(MemberRole.GENERAL)
        .modifiedAt(null)
        .unregisteredAt(null)
        .password(passwordEncoder.encode("password"))
        .isFirstLogIn(true)
        .build();
    memberRepository.save(otherMember);


    // 태그, 멤버태그
    fakeTag = Tag.builder().tagId(1L).name("태그1").build();
    fakeMemberTag = MemberTag.builder().memberTagId(1L).member(fakeMember).tag(fakeTag).build();

    tagRepository.save(fakeTag);
    memberTagRepository.save(fakeMemberTag);

  }





  @DisplayName("내 정보 가져오기 - 성공")
  @Test
  void getMyInfo_Success() throws Exception {


    mockMvc.perform(get("/user")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.memberId").value(fakeMember.getMemberId()))
        .andExpect(jsonPath("$.nickname").value("닉네임"))
        .andExpect(jsonPath("$.email").value("fakeMember@naver.com"))
        .andExpect(jsonPath("$.interests[0]").value("태그1"));

  }



  @DisplayName("내 정보 수정 - 성공(전체수정)")
  @Test
  void updateMyInfo_success_all() throws Exception {
    MemberUpdateDto.Request request = MemberUpdateDto.Request.builder()
        .nickname("pass새로운닉네임")
        .password("password")
        .newPassword1("1newPassword!")
        .newPassword2("1newPassword!")
        .build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.put("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk()) //400
        .andExpect(jsonPath("$.memberId").value(1L))
        .andExpect(jsonPath("$.nickname").value(request.getNickname()))
        .andExpect(jsonPath("$.interests[0]").value("태그1"));
  }



  @DisplayName("내 정보 수정 - 실패 (기존 비밀번호 불일치)")
  @Test
  void updateMyInfo_fail_NOT_CORRECT_PASSWORD() throws Exception {
    MemberUpdateDto.Request request = MemberUpdateDto.Request.builder()
        .nickname("새로운닉네임")
        .password("wrongPassword")
        .newPassword1("1newPassword!")
        .newPassword2("1newPassword!")
        .build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.put("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_CORRECT_PASSWORD.toString()))
    ;
  }



  @DisplayName("내 정보 수정 - 실패 (사용중인 닉네임)")
  @Test
  void updateMyInfo_fail_ALREADY_REGISTERED_NICKNAME() throws Exception {
    MemberUpdateDto.Request request = MemberUpdateDto.Request.builder()
        .nickname("새로운닉네임")
        .password("password")
        .build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.put("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ALREADY_REGISTERED_NICKNAME.toString()))
    ;
  }



  @DisplayName("회원탈퇴 - 성공")
  @Test
  void withdrawMember_success() throws Exception {
    WithdrawDto.Request request = new WithdrawDto.Request("password");

    String json = objectMapper.writeValueAsString(request); // 기본생성자가 없었어서 계속 400이 떳었음

    mockMvc.perform(MockMvcRequestBuilders.delete("/user/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk()) //400
        .andExpect(jsonPath("$.memberId").value(fakeMember.getMemberId()))
        .andExpect(jsonPath("$.email").value(fakeMember.getEmail()))
        .andExpect(jsonPath("$.nickname").value(fakeMember.getNickname()));
  }


  @DisplayName("회원탈퇴 - 실패(비밀번호 불일치)")
  @Test
  void withdrawMember() throws Exception {
    WithdrawDto.Request request = new WithdrawDto.Request("incorrectPassword");

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.delete("/user/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk()) //400
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_CORRECT_PASSWORD.toString()));
  }





  @DisplayName("관심주제 선택 - 성공")
  @Test
  void registerInterest_success() throws Exception {

    MemberInterestDto.Request request = new MemberInterestDto.Request(List.of("태그1"));
    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/interests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.memberId").value(1L))
        .andExpect(jsonPath("$.interests[0]").value("태그1"));
    ;
  }


  @DisplayName("관심주제 선택 - 실패(존재하지 않는 태그)")
  @Test
  void registerInterest_fail() throws Exception {

    MemberInterestDto.Request request = new MemberInterestDto.Request(List.of("태그1","없는태그"));
    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/interests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_TAG.toString()))
    ;
  }
}