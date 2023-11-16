package com.example.solumonbackend.member.service;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.solumonbackend.chat.repository.ChatMessageRepository;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.Member.MemberBuilder;
import com.example.solumonbackend.member.entity.Report;
import com.example.solumonbackend.member.model.ReportDto;
import com.example.solumonbackend.member.model.ReportDto.Request;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.ReportRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.member.type.ReportSubject;
import com.example.solumonbackend.member.type.ReportType;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.type.PostStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest_Report {


  @Mock
  private MemberRepository memberRepository;
  @Mock
  private ReportRepository reportRepository;

  @Mock
  private PostRepository postRepository;

  @Mock
  private ChatMessageRepository chatMessageRepository;


  @InjectMocks
  private MemberService memberService;

  private Member reporterMember;


  private MemberBuilder otherMemberBuilder;

//  private ReportDto.Request request;

  private Report report;


  @BeforeEach
  public void dataSetup() {


    reporterMember = Member.builder()
        .memberId(1L)
        .email("example1@naver.com")
        .nickname("nickname1")
        .registeredAt(LocalDateTime.now())
        .role(MemberRole.GENERAL)
        .modifiedAt(null)
        .unregisteredAt(null)
        .isFirstLogIn(true)
        .build();

    otherMemberBuilder = Member.builder()
        .memberId(2L)
        .email("example2@naver.com")
        .nickname("nickname2");



    report = Report.builder()
        .reportId(1L)
        .reporterId(reporterMember.getMemberId())
        .reportedAt(LocalDateTime.now())
        .reportSubject(ReportSubject.POST)
        .reportType(ReportType.OTHER)
        .reportExplanation("신고합니다")
        .build();

  }


  @DisplayName("게시글신고 - 성공( 내가 해당유저를 신고한적은 있으나 3일이 지남)")
  @Test
  void reportMember_success_reportablePeriod() {

    // Given
    Member otherMember = otherMemberBuilder.build();

    Post post = Post.builder()
        .postId(1L)
        .member(otherMember)
        .build();


    ReportDto.Request request = Request.builder()
        .reportedMemberId(otherMember.getMemberId())
        .postId(post.getPostId())
        .reportSubject(ReportSubject.POST)
        .reportType(ReportType.OTHER)
        .reportExplanation("신고합니다")
        .build();

    when(postRepository.findById(request.getPostId())).thenReturn(Optional.of(post));
    when(memberRepository.findById(request.getReportedMemberId())).thenReturn(Optional.of(otherMember));
    when(reportRepository.findTopByMemberMemberIdAndReporterIdOrderByReportedAtDesc(
        otherMember.getMemberId(), reporterMember.getMemberId()))
        .thenReturn(Optional.of(Report.builder()
            .reportId(1L)
            .member(otherMember)
            .reporterId(reporterMember.getMemberId())
            .reportedAt(LocalDateTime.now().minusDays(4)) // 가장 최근의 신고날짜가 4일전
            .build()));

    when(postRepository.save(post)).thenReturn(post);
    when(reportRepository.save(any())).thenReturn(report);

    ArgumentCaptor<Report> reportArgumentCaptor = ArgumentCaptor.forClass(Report.class);
    ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);

    // When
    memberService.reportMember(reporterMember, request);

    // Then
    verify(memberRepository, times(1)).findById(request.getReportedMemberId());
    verify(postRepository,times(1)).findById(request.getPostId());
    verify(postRepository,times(1)).save(postArgumentCaptor.capture());
    verify(reportRepository, times(1)).save(reportArgumentCaptor.capture());

    Post captorPost = postArgumentCaptor.getValue();
    Report captorReport = reportArgumentCaptor.getValue();

    assertEquals(post.getPostId(),captorPost.getPostId());
    assertEquals(PostStatus.REPORTED, captorPost.getPostStatus());
    assertEquals(post.getPostId(),captorReport.getPostId());

  }


  @DisplayName("유저신고 - 성공(해당 유저를 신고한 이력이 없음)")
  @Test
  void reportMember_success_firstReport() {

    // Given
    Member otherMember = otherMemberBuilder.build();

    Post post = Post.builder()
        .postId(1L)
        .member(otherMember)
        .build();


    ReportDto.Request request = Request.builder()
        .reportedMemberId(otherMember.getMemberId())
        .postId(post.getPostId())
        .reportSubject(ReportSubject.POST)
        .reportType(ReportType.OTHER)
        .reportExplanation("신고합니다")
        .build();

    when(memberRepository.findById(request.getReportedMemberId())).thenReturn(Optional.of(otherMember));
    when(reportRepository.findTopByMemberMemberIdAndReporterIdOrderByReportedAtDesc(
        otherMember.getMemberId(), reporterMember.getMemberId()))
        .thenReturn(Optional.empty()); // 신고한 적 없음

    when(postRepository.findById(request.getPostId())).thenReturn(Optional.of(post));

    post.reportPost();
    when(postRepository.save(post)).thenReturn(post);
    when(reportRepository.save(any())).thenReturn(report);

    ArgumentCaptor<Report> reportArgumentCaptor = ArgumentCaptor.forClass(Report.class);
    ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);

    // When
    memberService.reportMember(reporterMember, request);

    // Then
    verify(memberRepository, times(1)).findById(request.getReportedMemberId());
    verify(postRepository,times(1)).findById(request.getPostId());
    verify(postRepository,times(1)).save(postArgumentCaptor.capture());
    verify(reportRepository, times(1)).save(reportArgumentCaptor.capture());

    Post captorPost = postArgumentCaptor.getValue();
    Report captorReport = reportArgumentCaptor.getValue();

    assertEquals(post.getPostId(),captorPost.getPostId());
    assertEquals(PostStatus.REPORTED, captorPost.getPostStatus());
    assertEquals(post.getPostId(),captorReport.getPostId());
  }

  @DisplayName("유저신고 - 실패(존재하지 않는 유저)")
  @Test
  void reportMember_fail_NOT_FOUND_MEMBER() {

    // Given
    Post post = Post.builder()
        .postId(1L)
        .build();

    ReportDto.Request request = Request.builder()
        .reportedMemberId(-1L)
        .postId(post.getPostId())
        .reportSubject(ReportSubject.POST)
        .reportType(ReportType.OTHER)
        .reportExplanation("신고합니다")
        .build();

    when(postRepository.findById(request.getPostId())).thenReturn(Optional.of(post));
    when(memberRepository.findById(-1L)).thenReturn(Optional.empty());

    // When
    MemberException memberException
        = assertThrows(MemberException.class,
        () -> memberService.reportMember(reporterMember, request));

    // Then
    verify(memberRepository, times(1)).findById(request.getReportedMemberId());
    assertEquals(ErrorCode.NOT_FOUND_MEMBER, memberException.getErrorCode());

  }

  @DisplayName("유저신고 - 실패(탈퇴한 멤버)")
  @Test
  void reportMember_fail_unregisteredMember() {

    // Given
    Member otherMember = otherMemberBuilder.unregisteredAt(LocalDateTime.now()).build();

    Post post = Post.builder()
        .postId(1L)
        .build();

    ReportDto.Request request = Request.builder()
        .reportedMemberId(-1L)
        .postId(post.getPostId())
        .reportSubject(ReportSubject.POST)
        .reportType(ReportType.OTHER)
        .reportExplanation("신고합니다")
        .build();

    when(postRepository.findById(request.getPostId())).thenReturn(Optional.of(post));
    when(memberRepository.findById(request.getReportedMemberId())).thenReturn(Optional.of(otherMember)); // 탈퇴한 멤버

    // When
    MemberException memberException
        = assertThrows(MemberException.class,
        () -> memberService.reportMember(reporterMember, request));

    // Then
    verify(memberRepository, times(1)).findById(request.getReportedMemberId());
    assertEquals(ErrorCode.UNREGISTERED_MEMBER, memberException.getErrorCode());

  }


  @DisplayName("유저신고 - 실패(현재 정지상태인 유저)")
  @Test
  void reportMember_fail_ALREADY_BANNED_MEMBER() {

    // Given
    Member otherMember = otherMemberBuilder.bannedAt(LocalDateTime.now()).build();

    Post post = Post.builder()
        .postId(1L)
        .build();

    ReportDto.Request request = Request.builder()
        .reportedMemberId(-1L)
        .postId(post.getPostId())
        .reportSubject(ReportSubject.POST)
        .reportType(ReportType.OTHER)
        .reportExplanation("신고합니다")
        .build();

    when(postRepository.findById(request.getPostId())).thenReturn(Optional.of(post));
    when(memberRepository.findById(request.getReportedMemberId())).thenReturn(Optional.of(otherMember));

    // When
    MemberException memberException
        = assertThrows(MemberException.class,
        () -> memberService.reportMember(reporterMember, request));

    // Then
    verify(memberRepository, times(1)).findById(request.getReportedMemberId());
    assertEquals(ErrorCode.ALREADY_BANNED_MEMBER, memberException.getErrorCode());

  }


  @DisplayName("유저신고 - 실패(신고가능 기간이 아님)")
  @Test
  void reportMember_fail_COOL_TIME_REPORT_MEMBER() {
    // Given
    Member otherMember = otherMemberBuilder.build();

    Post post = Post.builder()
        .postId(1L)
        .build();

    ReportDto.Request request = Request.builder()
        .reportedMemberId(-1L)
        .postId(post.getPostId())
        .reportSubject(ReportSubject.POST)
        .reportType(ReportType.OTHER)
        .reportExplanation("신고합니다")
        .build();

    when(postRepository.findById(request.getPostId())).thenReturn(Optional.of(post));
    when(memberRepository.findById(request.getReportedMemberId())).thenReturn(Optional.of(otherMember));
    when(reportRepository.findTopByMemberMemberIdAndReporterIdOrderByReportedAtDesc(
        otherMember.getMemberId(), reporterMember.getMemberId()))
        .thenReturn(Optional.of(Report.builder()
            .reportId(1L)
            .member(otherMember)
            .reporterId(reporterMember.getMemberId())  // 내가 신고했고
            .reportedAt(LocalDateTime.now().minusDays(1))  // 아직 3일이 지나지 않음
            .build()));

    // When
    MemberException memberException
        = assertThrows(MemberException.class,
        () -> memberService.reportMember(reporterMember, request));

    // Then
    verify(memberRepository, times(1)).findById(request.getReportedMemberId());
    assertEquals(ErrorCode.COOL_TIME_REPORT_MEMBER, memberException.getErrorCode());
  }


//  @DisplayName("유저 정지 - 영구정지")
//  @Test
//  void banMember_PERMANENT_BAN() {
//
//    when(reportRepository.countByMember_MemberId(reporterMember.getMemberId())).thenReturn(15);
//    when(memberRepository.save(reporterMember)).thenReturn(reporterMember);
//    ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);
//
//    // When
//    memberService.banMember(reporterMember);
//
//    // Then
//    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());
//    Member captorValue = memberArgumentCaptor.getValue();
//
//    assertEquals(MemberRole.PERMANENT_BAN.value(), captorValue.getRole().value());
//
//  }
//
//  @DisplayName("유저 정지 - 정지")
//  @Test
//  void banMember_BAN() {
//
//    when(reportRepository.countByMember_MemberId(reporterMember.getMemberId())).thenReturn(5);
//    when(memberRepository.save(reporterMember)).thenReturn(reporterMember);
//    ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);
//
//    // When
//    memberService.banMember(reporterMember);
//
//    // Then
//    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());
//    Member captorValue = memberArgumentCaptor.getValue();
//
//    assertEquals(MemberRole.BANNED.value(), captorValue.getRole().value());
//
//  }
//
//
//  @DisplayName("유저 정지 - 정지조건 미충족")
//  @Test
//  void banMember_fail() {
//
//    when(reportRepository.countByMember_MemberId(reporterMember.getMemberId())).thenReturn(7);
//    when(memberRepository.save(reporterMember)).thenReturn(reporterMember);
//    ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);
//
//    // When
//    memberService.banMember(reporterMember);
//
//    // Then
//    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());
//    Member captorValue = memberArgumentCaptor.getValue();
//
//    assertEquals(MemberRole.GENERAL.value(), captorValue.getRole().value());
//
//  }
//
}