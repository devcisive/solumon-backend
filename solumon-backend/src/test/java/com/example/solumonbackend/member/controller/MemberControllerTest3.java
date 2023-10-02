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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
class MemberControllerTest3 {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private ReportRepository reportRepository;


  private Map<String, Member> fakeMembers;
  private List<Report> reports;

  private ReportDto.Request request;


  @BeforeEach
  public void dataSetup() {
    // 멤버(신고자)
    // 탈퇴한 멤버
    // 정지상태인 멤버

    // 신고한 적 없는 멤버
    // 신고한지 3일 지난 멤버
    // 신고한지 3일 지나지 않은 멤버

    LocalDateTime of = LocalDateTime.of(2023, 04, 01, 0, 0);


    fakeMembers = new HashMap<>();
    fakeMembers.put("reporter", Member.builder().memberId(1L).email("reporter@gmail.com").isFirstLogIn(false).build());
    fakeMembers.put("withdrawnMember",Member.builder().memberId(2L).email("withdraw@gmail.com").unregisteredAt(LocalDateTime.now()).isFirstLogIn(false).build());
    fakeMembers.put("bannedMember",Member.builder().memberId(3L).email("banned@gmail.com").role(MemberRole.BANNED).bannedAt(of).isFirstLogIn(false).build());
    fakeMembers.put("nonReportingMember", Member.builder().memberId(4L).email("nonReporting@gmail.com").isFirstLogIn(false).build());


    fakeMembers.put("reportedMoreThan3DaysAgoMember", Member.builder().memberId(5L).email("3DaysAgo@gmail.com").isFirstLogIn(false).build());
    fakeMembers.put("reportedWithIn3DaysMember", Member.builder().memberId(6L).email("WithIn3Days@gmail.com").isFirstLogIn(false).build());

    memberRepository.saveAll(new ArrayList<>(fakeMembers.values()));
    // 5,6번 데이터는 저장이 안되는 것으로 확인됨
    // 멤버의 날짜값 저장이 안됨 (report의 날짜값은 저장 됨)
    


    // 내가 신고한지 3일이 지난 신고데이터
    reports = new ArrayList<>();
    reports.add(Report.builder()
        .reportId(1L)
//        .member(fakeMembers.get("reportedMoreThan3DaysAgoMember")) // 찾을 수 없는 데이터라 에러가 뜸
          .member(fakeMembers.get("reporter")) // 저장이 안되는 이슈때문에 1번으로 대신 넣었음
        .reporterId(fakeMembers.get("reporter").getMemberId())
        .reportedAt(LocalDateTime.now().minusDays(7))
        .build());



    // 내가 신고한지 3일이 지나지않은 신고데이터
    reports.add(Report.builder()
        .reportId(2L)
//        .member(fakeMembers.get("reportedWithIn3DaysMember")) // 찾을 수 없는 데이터라 에러가 뜸
            .member(fakeMembers.get("reporter")) // 저장이 안되는 이슈때문에 1번으로 대신 넣었음
        .reporterId(fakeMembers.get("reporter").getMemberId())
        .reportedAt(LocalDateTime.now().minusDays(2))
        .build());

    reportRepository.saveAll(reports);  // 5번 6번 데이터를 찾을 수 없다고 안된다.


    request = new ReportDto.Request(ReportType.OTHER,"신고합니다");


  }



  @DisplayName("유저 신고 - 성공(내가 신고한 적 없는 유저)")
  @Test
  @WithUserDetails(value = "reporter@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void reportMember_success_firstReport() throws Exception {

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/" + fakeMembers.get("nonReportingMember").getMemberId() +"/report")
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

    mockMvc.perform(post("/user/" + fakeMembers.get("reporter").getMemberId() +"/report")  // 원래는 "reportedMoreThan3DaysAgoMember"
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

    mockMvc.perform(post("/user/100/report")
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

    mockMvc.perform(post("/user/" + fakeMembers.get("withdrawnMember").getMemberId() +"/report")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_MEMBER.toString()));
  }


  // 멤버에 날짜값 저장이 안되는 이슈로 통과 X
  @DisplayName("유저신고 - 실패(현재 정지상태인 유저)")
  @Test
  @WithUserDetails(value = "reporter@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void reportMember_fail_ALREADY_BANNED_MEMBER() throws Exception {

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/" + fakeMembers.get("bannedMember").getMemberId() +"/report") //bannedMember
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

    mockMvc.perform(post("/user/" + fakeMembers.get("reporter").getMemberId() +"/report") //reportedWithIn3DaysMember
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.COOL_TIME_REPORT_MEMBER.toString()));
  }
}