package com.example.solumonbackend.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.Report;
import com.example.solumonbackend.member.model.ReportDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.ReportRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.member.type.ReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest_Report {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private ReportRepository reportRepository;




  private ReportDto.Request request;


  private Member reporterMember ;
  private Member withdrawnMember ;
  private Member bannedMember ;
  private Member nonReportingMember ;
  private Member reportedPossibleDaysMember;
  private Member reportedCoolTimeMember;
  private List<Report> reports;


  @BeforeEach
  public void dataSetup() {

    LocalDateTime of = LocalDateTime.of(2023, 04, 01, 0, 0);

    reporterMember = Member.builder()
        .nickname("신고자")
        .email("reporter@gmail.com")
        .isFirstLogIn(false)
        .build();

    withdrawnMember = Member.builder()
        .nickname("탈퇴멤버")
        .email("withdraw@gmail.com")
        .unregisteredAt(LocalDateTime.now())
        .isFirstLogIn(false)
        .build();

    bannedMember = Member.builder()
        .nickname("정지멤버")
        .email("banned@gmail.com")
        .role(MemberRole.BANNED)
        .bannedAt(of)
        .isFirstLogIn(false)
        .build();

    nonReportingMember = Member.builder()
        .nickname("내가신고한적없는멤버")
        .email("nonReporting@gmail.com")
        .isFirstLogIn(false)
        .build();

    reportedPossibleDaysMember = Member.builder()
        .nickname("신고가능기간멤버")
        .email("DaysAgo@gmail.com")
        .isFirstLogIn(false)
        .build();

    reportedCoolTimeMember = Member.builder()
        .nickname("신고아직못하는멤버")
        .email("WithInDays@gmail.com")
        .isFirstLogIn(false)
        .build();

    memberRepository.saveAll(List.of(reporterMember,withdrawnMember,bannedMember,nonReportingMember,reportedPossibleDaysMember,reportedCoolTimeMember));



    // 내가 신고한지 3일이 지난 신고데이터
    reports = new ArrayList<>();
    reports.add(Report.builder()
        .reportId(1L)
        .member(reportedPossibleDaysMember)
        .reporterId(reporterMember.getMemberId())
        .reportedAt(LocalDateTime.now().minusDays(7))
        .build());



    // 내가 신고한지 3일이 지나지않은 신고데이터
    reports.add(Report.builder()
        .reportId(2L)
        .member(reportedCoolTimeMember)
        .reporterId(reporterMember.getMemberId())
        .reportedAt(LocalDateTime.now().minusDays(2))
        .build());

    reportRepository.saveAll(reports);


    request = new ReportDto.Request(ReportType.OTHER,"신고합니다");


  }



  @DisplayName("유저 신고 - 성공(내가 신고한 적 없는 유저)")
  @Test
  @WithUserDetails(value = "reporter@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void reportMember_success_firstReport() throws Exception {

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/report")
            .param("nickname", nonReportingMember.getNickname())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk());
  }


  @DisplayName("유저 신고 - 성공(신고한 적은 있으나 3일이 지난 유저)")
  @Test
  @WithUserDetails(value = "reporter@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void reportMember_success_reportablePeriod() throws Exception {

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/report")
            .param("nickname", reportedPossibleDaysMember.getNickname())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk());
  }


  @DisplayName("유저신고 - 실패(존재하지 않는 유저)")
  @Test
  @WithUserDetails(value = "reporter@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void reportMember_fail_NOT_FOUND_MEMBER() throws Exception {

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/report")
            .param("nickname","없는 멤버")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_MEMBER.toString()));
  }


  @DisplayName("유저신고 - 실패(탈퇴한 멤버)")
  @Test
  @WithUserDetails(value = "reporter@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void reportMember_fail_unregisteredMember() throws Exception {

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/report")
            .param("nickname",withdrawnMember.getNickname())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.UNREGISTERED_MEMBER.toString()));
  }



  @DisplayName("유저신고 - 실패(현재 정지상태인 유저)")
  @Test
  @WithUserDetails(value = "reporter@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void reportMember_fail_ALREADY_BANNED_MEMBER() throws Exception {

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/report")
            .param("nickname",bannedMember.getNickname())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ALREADY_BANNED_MEMBER.toString()));
  }


  @DisplayName("유저신고 - 실패(신고가능 기간이 아님)")
  @Test
  @WithUserDetails(value = "reporter@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void reportMember_fail_COOL_TIME_REPORT_MEMBER() throws Exception {

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/report")
            .param("nickname",reportedCoolTimeMember.getNickname())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.COOL_TIME_REPORT_MEMBER.toString()));
  }
}