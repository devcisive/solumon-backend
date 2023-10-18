package com.example.solumonbackend.member.service;

import static com.example.solumonbackend.global.exception.ErrorCode.NOT_CORRECT_PASSWORD;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_FOUND_TAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.exception.TagException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberLogDto.Info;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.MemberUpdateDto.Request;
import com.example.solumonbackend.member.model.MemberUpdateDto.Response;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.MyParticipatePostDto;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;


@ExtendWith(MockitoExtension.class)
class MemberServiceTest_MyInfo {

  @Mock
  public PasswordEncoder passwordEncoder;
  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PostRepository postRepository;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private MemberTagRepository memberTagRepository;

  @InjectMocks
  private MemberService memberService;


  private Member fakeMember;
  private List<MemberTag> fakeMemberTags;

  private List<Tag> fakeTags;

  @BeforeEach
  public void dataSetup() {

    fakeMember = Member.builder()
        .memberId(1L)
        .kakaoId(1L)
        .email("example@naver.com")
        .nickname("nickname")
        .registeredAt(LocalDateTime.now())
        .role(MemberRole.GENERAL)
        .modifiedAt(null)
        .unregisteredAt(null)
        .password(passwordEncoder.encode("password"))
        .isFirstLogIn(true)
        .build();


    fakeTags = Arrays.asList(
        Tag.builder().tagId(1L).name("태그1").build(),
        Tag.builder().tagId(2L).name("태그2").build()
    );


    AtomicLong memberTagIdGenerator = new AtomicLong();
    fakeMemberTags = fakeTags.stream().map(tag ->
        MemberTag.builder()
            .memberTagId(memberTagIdGenerator.incrementAndGet())
            .member(fakeMember)
            .tag(tag)
            .build()).collect(Collectors.toList());

  }


  @DisplayName("내 정보 조회 성공")
  @Test
  public void testGetMyInfo() {
    // Given

    when(memberTagRepository.findAllByMember_MemberId(fakeMember.getMemberId()))
        .thenReturn(fakeMemberTags);

    // When
    Info infoResult = memberService.getMyInfo(fakeMember);

    // Then
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(fakeMember.getMemberId());

    assertEquals(fakeMember.getMemberId(), infoResult.getMemberId());
    assertEquals(fakeMember.getNickname(), infoResult.getNickname());
    assertEquals(fakeMember.getEmail(), infoResult.getEmail());
    assertIterableEquals(List.of("태그1", "태그2"), infoResult.getInterests());

  }


  @DisplayName("내 정보 수정 성공 - 닉네임만 수정")
  @Test
  void updateMyInfoTest_success_nickname() {
    // Given

    MemberUpdateDto.Request request = Request.builder()
        .nickname("newNickname")
        .password("password")
        .newPassword(null)
        .build();

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(true);
    when(memberRepository.existsByNickname(request.getNickname())).thenReturn(false);

    when(memberTagRepository.findAllByMember_MemberId(fakeMember.getMemberId())).thenReturn(
        fakeMemberTags);
    ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);

    // When
    Response response = memberService.updateMyInfo(fakeMember, request);

    // Then
    verify(passwordEncoder, times(1)).matches(request.getPassword(), fakeMember.getPassword());
    verify(memberRepository, times(1)).existsByNickname(request.getNickname());
    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(fakeMember.getMemberId());

    assertEquals(request.getNickname(), response.getNickname());
    assertIterableEquals(List.of("태그1", "태그2"), response.getInterests());
  }


  @DisplayName("내 정보 수정 성공 - 비밀번호만 수정")
  @Test
  void updateMyInfoTest_success_password() {
    // Given

    MemberUpdateDto.Request request = Request.builder()
        .nickname("nickname")
        .password("password")
        .newPassword("new_password!")
        .build();

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(true);
    when(memberRepository.save(fakeMember)).thenReturn(fakeMember);
    when(memberTagRepository.findAllByMember_MemberId(fakeMember.getMemberId())).thenReturn(
        fakeMemberTags);

    ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);

    // When
    Response response = memberService.updateMyInfo(fakeMember, request);

    // Then
    verify(passwordEncoder, times(1)).matches(request.getPassword(), fakeMember.getPassword());
    verify(passwordEncoder, times(1)).encode(request.getNewPassword());

    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());

    assertEquals(fakeMember.getMemberId(), response.getMemberId());
    assertEquals(request.getNickname(), response.getNickname());
    assertIterableEquals(List.of("태그1", "태그2"), response.getInterests());


  }

  @DisplayName("내 정보 수정 성공 - 닉네임, 비밀번호 수정")
  @Test
  void updateMyInfoTest_success_All() {
    // Given

    MemberUpdateDto.Request request = Request.builder()
        .nickname("newNickname")
        .password("password")
        .newPassword("newPassword")
        .build();

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(true);
    when(memberRepository.existsByNickname(request.getNickname())).thenReturn(false);

    when(memberTagRepository.findAllByMember_MemberId(fakeMember.getMemberId())).thenReturn(
        fakeMemberTags);
    ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);

    // When
    Response response = memberService.updateMyInfo(fakeMember, request);

    // Then
    verify(passwordEncoder, times(1)).matches(request.getPassword(), fakeMember.getPassword());
    verify(passwordEncoder, times(1)).encode(request.getNewPassword());

    verify(memberRepository, times(1)).existsByNickname(request.getNickname());
    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());
    Member captorValue = memberArgumentCaptor.getValue();

    assertEquals(request.getNickname(), response.getNickname());
    assertEquals(passwordEncoder.encode(request.getNewPassword()), captorValue.getPassword());
    assertIterableEquals(List.of("태그1", "태그2"), response.getInterests());
  }


  @DisplayName("내 정보 수정 실패 - 사용중인 닉네임 ")
  @Test
  void updateMyInfo_fail_AlreadyRegisteredNickname() {
    // Given

    MemberUpdateDto.Request request = Request.builder()
        .nickname("nickname1")
        .password("incorrect_password")
        .newPassword("new_password!")
        .build();

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(
        true);
    when(memberRepository.existsByNickname(request.getNickname())).thenReturn(true);

    // When
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberService.updateMyInfo(fakeMember, request));

    // Then
    verify(passwordEncoder, times(1)).matches(request.getPassword(), fakeMember.getPassword());
    verify(memberRepository, times(1)).existsByNickname(request.getNickname());

    assertEquals(ErrorCode.ALREADY_REGISTERED_NICKNAME, memberException.getErrorCode());

  }


  @DisplayName("내 정보 수정 실패 - 기존비밀번호 불일치 ")
  @Test
  void updateMyInfo_fail_PasswordNotMatching() {
    // Given

    MemberUpdateDto.Request request = Request.builder()
        .nickname("nickname1")
        .password("incorrect_password")
        .newPassword("new_password!")
        .build();

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(
        false);

    // When
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberService.updateMyInfo(fakeMember, request));

    // Then
    assertEquals(ErrorCode.NOT_CORRECT_PASSWORD, memberException.getErrorCode());


  }



  @DisplayName("회원탈퇴")
  @Test
  void withdrawMember_success() {

    // Given
    WithdrawDto.Request request = new WithdrawDto.Request("password");

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(true);
    when(memberRepository.save(fakeMember)).thenReturn(fakeMember);
    ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);

    // When
    WithdrawDto.Response response = memberService.withdrawMember(fakeMember, request);

    // Then
    verify(passwordEncoder, times(1)).matches(request.getPassword(), fakeMember.getPassword());
    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());
    Member captorValue = memberArgumentCaptor.getValue();

    assertEquals(fakeMember.getUnregisteredAt(), captorValue.getUnregisteredAt());
    assertEquals(fakeMember.getMemberId(), response.getMemberId());
    assertEquals(fakeMember.getEmail(), response.getEmail());
    assertEquals(fakeMember.getNickname(), response.getNickname());


  }


  @DisplayName("회원탈퇴 실패")
  @Test
  void withdrawMember_fail() {

    // Given
    WithdrawDto.Request request = new WithdrawDto.Request("incorrect_password");
    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(
        false);

    // When
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberService.withdrawMember(fakeMember, request));

    // Then
    verify(passwordEncoder, times(1)).matches(request.getPassword(), fakeMember.getPassword());
    assertEquals(NOT_CORRECT_PASSWORD, memberException.getErrorCode());

  }


  @DisplayName("내 관심주제 설정 - 성공")
  @Test
  void registerInterest_success() {
    // Given

    MemberInterestDto.Request request = new MemberInterestDto.Request(List.of("태그1", "태그2"));


    when(tagRepository.findByName("태그1")).thenReturn(Optional.of(fakeTags.get(0)));
    when(tagRepository.findByName("태그2")).thenReturn(Optional.of(fakeTags.get(1)));
    when(memberTagRepository.saveAll(any())).thenReturn(fakeMemberTags);

    // When
    MemberInterestDto.Response response = memberService.registerInterest(fakeMember, request);

    // Then
    verify(memberTagRepository, times(1)).deleteAllByMember_MemberId(fakeMember.getMemberId());
    verify(tagRepository, times(2)).findByName(any());
    verify(memberTagRepository, times(1)).saveAll(any());

    assertEquals(fakeMember.getMemberId(), response.getMemberId());
    assertFalse(fakeMember.isFirstLogIn());
    assertIterableEquals(List.of("태그1", "태그2"), response.getInterests());

  }


  @DisplayName("내 관심주제 설정 실패 - 존재하지 않는 태그")
  @Test
  void registerInterest_fail() {
    // Given

    MemberInterestDto.Request request = new MemberInterestDto.Request(List.of("관심주제1", "관심주제2"));

    when(tagRepository.findByName(any())).thenReturn(Optional.empty());

    // When
    TagException tagException = assertThrows(TagException.class,
        () -> memberService.registerInterest(fakeMember, request));

    // Then
    assertEquals(NOT_FOUND_TAG, tagException.getErrorCode());

    verify(memberTagRepository, times(1)).deleteAllByMember_MemberId(fakeMember.getMemberId());
    verify(memberTagRepository, never()).saveAll(any());

  }


  @DisplayName("나의 활동게시글 조회")
  @Test
  void getMyParticipatePosts() {
    //Given

    PostStatus postStatus = PostStatus.ONGOING;
    PostParticipateType postParticipateType = PostParticipateType.CHAT;
    PostOrder postOrder = PostOrder.LATEST;
    Pageable pageable = Pageable.ofSize(10);

    List<MyParticipatePostDto> fakeMyParticipatePosts = creatFakeMyParticipatePosts();

    when(postRepository.getMyParticipatePostPages(
        eq(fakeMember.getMemberId()),
        eq(postParticipateType),
        eq(postStatus),
        eq(postOrder),
        eq(pageable)
    )).thenReturn(new PageImpl<>(fakeMyParticipatePosts, pageable, fakeMyParticipatePosts.size()));

    // When
    Page<MyParticipatePostDto> result = memberService.getMyParticipatePosts(
        fakeMember, postStatus, postParticipateType, postOrder, pageable);

    // Then
    assertEquals(fakeMyParticipatePosts.size(), result.getContent().size());

  }


  private List<MyParticipatePostDto> creatFakeMyParticipatePosts() {
    List<MyParticipatePostDto> fakeData = new ArrayList<>();

    MyParticipatePostDto post1 = MyParticipatePostDto.builder()
        .postId(1L)
        .nickname("작성자1")
        .title("제목1")
        .contents("내용1")
        .createdAt(LocalDateTime.now())
        .thumbnailUrl("이미지1")
        .voteCount(4)
        .chatCount(20)
        .build();

    MyParticipatePostDto post2 = MyParticipatePostDto.builder()
        .postId(2L)
        .nickname("작성자2")
        .title("제목2")
        .contents("내용2")
        .createdAt(LocalDateTime.now().plusDays(1))
        .thumbnailUrl("이미지2")
        .voteCount(4)
        .chatCount(20)
        .build();

    fakeData.add(post1);
    fakeData.add(post2);

    return fakeData;
  }


}