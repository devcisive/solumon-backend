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
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberLogDto.Info;
import com.example.solumonbackend.member.model.MemberTestData;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.MemberUpdateDto.Request;
import com.example.solumonbackend.member.model.MemberUpdateDto.Response;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.MyParticipatePostDto;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostState;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
class MemberServiceTest2 {

  @Mock
  public PasswordEncoder passwordEncoder;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private RefreshTokenRedisRepository refreshTokenRedisRepository;
  @Mock
  private PostRepository postRepository;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private MemberTagRepository memberTagRepository;

  @InjectMocks
  private MemberService memberService;


  @DisplayName("내 정보 조회 성공")
  @Test
  public void testGetMyInfo() {

    // Given
    Member fakeMember = MemberTestData.fakeMemberBuilder().build();

    List<Tag> fakeTags = Arrays.asList(
        Tag.builder().tagId(1L).name("태그1").build(),
        Tag.builder().tagId(2L).name("태그2").build()
    );
    List<MemberTag> fakeMemberTags = MemberTestData.createMemberTags(fakeMember, fakeTags);

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
    Member fakeMember = MemberTestData.fakeMemberBuilder().nickname("nickname").password("password")
        .build();

    List<Tag> fakeTags = Arrays.asList(
        Tag.builder().tagId(1L).name("태그1").build(),
        Tag.builder().tagId(2L).name("태그2").build()
    );

    List<MemberTag> fakeMemberTags = MemberTestData.createMemberTags(fakeMember, fakeTags);

    MemberUpdateDto.Request request = Request.builder()
        .nickname("newNickname")
        .password("password")
        .newPassword1(null)
        .newPassword2(null)
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

    Member fakeMember = MemberTestData.fakeMemberBuilder()
        .nickname("nickname")
        .password(passwordEncoder.encode("password"))
        .build();

    List<Tag> fakeTags = Arrays.asList(
        Tag.builder().tagId(1L).name("태그1").build(),
        Tag.builder().tagId(2L).name("태그2").build()
    );

    List<MemberTag> fakeMemberTags = MemberTestData.createMemberTags(fakeMember, fakeTags);

    MemberUpdateDto.Request request = Request.builder()
        .nickname("nickname")
        .password("password")
        .newPassword1("new_password!")
        .newPassword2("new_password!")
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
    verify(passwordEncoder, times(1)).encode(request.getNewPassword1());

    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());

    assertEquals(fakeMember.getMemberId(), response.getMemberId());
    assertEquals(request.getNickname(), response.getNickname());
    assertIterableEquals(List.of("태그1", "태그2"), response.getInterests());


  }

  @DisplayName("내 정보 수정 성공 - 닉네임, 비밀번호 수정")
  @Test
  void updateMyInfoTest_success_All() {
    // Given
    Member fakeMember = MemberTestData.fakeMemberBuilder()
        .nickname("nickname")
        .password(passwordEncoder.encode("password"))
        .build();

    List<Tag> fakeTags = Arrays.asList(
        Tag.builder().tagId(1L).name("태그1").build(),
        Tag.builder().tagId(2L).name("태그2").build()
    );

    List<MemberTag> fakeMemberTags = MemberTestData.createMemberTags(fakeMember, fakeTags);

    MemberUpdateDto.Request request = Request.builder()
        .nickname("newNickname")
        .password("password")
        .newPassword1("newPassword")
        .newPassword2("newPassword")
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
    verify(passwordEncoder, times(1)).encode(request.getNewPassword1());

    verify(memberRepository, times(1)).existsByNickname(request.getNickname());
    verify(memberRepository, times(1)).save(memberArgumentCaptor.capture());
    Member captorValue = memberArgumentCaptor.getValue();

    assertEquals(request.getNickname(), response.getNickname());
    assertEquals(passwordEncoder.encode(request.getNewPassword1()), captorValue.getPassword());
    assertIterableEquals(List.of("태그1", "태그2"), response.getInterests());
  }


  @DisplayName("내 정보 수정 실패 - 사용중인 닉네임 ")
  @Test
  void updateMyInfo_fail_AlreadyRegisteredNickname() {
    // Given
    Member fakeMember = MemberTestData.fakeMemberBuilder().nickname("nickname").password("password")
        .build();

    MemberUpdateDto.Request request = Request.builder()
        .nickname("nickname1")
        .password("incorrect_password")
        .newPassword1("new_password!")
        .newPassword2("new_password!")
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
    Member fakeMember = MemberTestData.fakeMemberBuilder().password("password").build();

    MemberUpdateDto.Request request = Request.builder()
        .nickname("nickname1")
        .password("incorrect_password")
        .newPassword1("new_password!")
        .newPassword2("new_password!")
        .build();

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(
        false);

    // When
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberService.updateMyInfo(fakeMember, request));

    // Then
    assertEquals(ErrorCode.NOT_CORRECT_PASSWORD, memberException.getErrorCode());


  }


  @DisplayName("내 정보 수정 실패 - 새 비밀번호1,2 불일치 ")
  @Test
  void updateMyInfo_fail_PasswordMatching_Password1And2NotMatching() {
    // Given

    Member fakeMember = MemberTestData.fakeMemberBuilder().build();

    MemberUpdateDto.Request request = Request.builder()
        .nickname("nickname1")
        .password("password")
        .newPassword1("new_password")
        .newPassword2("new_password2")
        .build();

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(true);

    // When
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberService.updateMyInfo(fakeMember, request));

    // Then
    assertEquals(ErrorCode.NOT_CORRECT_NEW_PASSWORD, memberException.getErrorCode());

  }


  @DisplayName("회원탈퇴")
  @Test
  void withdrawMember_success() {

    // Given
    WithdrawDto.Request request = new WithdrawDto.Request("password");
    Member fakeMember = MemberTestData.fakeMemberBuilder().password("password").build();
    ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);

    when(passwordEncoder.matches(request.getPassword(), fakeMember.getPassword())).thenReturn(true);
    when(memberRepository.save(fakeMember)).thenReturn(fakeMember);

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
    Member fakeMember = MemberTestData.fakeMemberBuilder().password("password").build();

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
    Member fakeMember = MemberTestData.fakeMemberBuilder().isFirstLogIn(true).build();

    MemberInterestDto.Request request = new MemberInterestDto.Request(List.of("관심주제1", "관심주제2"));

    List<Tag> fakeTags = Arrays.asList(
        Tag.builder().tagId(1L).name("관심주제1").build(),
        Tag.builder().tagId(2L).name("관심주제2").build());

    List<MemberTag> fakeMemberTags = MemberTestData.createMemberTags(fakeMember, fakeTags);

    when(tagRepository.findByName("관심주제1")).thenReturn(Optional.of(fakeTags.get(0)));
    when(tagRepository.findByName("관심주제2")).thenReturn(Optional.of(fakeTags.get(1)));
    when(memberTagRepository.saveAll(any())).thenReturn(fakeMemberTags);
    when(memberTagRepository.findAllByMember_MemberId(fakeMember.getMemberId())).thenReturn(
        fakeMemberTags);

    // When
    MemberInterestDto.Response response = memberService.registerInterest(fakeMember, request);

    // Then
    verify(memberTagRepository, times(1)).deleteAllByMember_MemberId(fakeMember.getMemberId());
    verify(tagRepository, times(2)).findByName(any());
    verify(memberTagRepository, times(1)).saveAll(any());
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(fakeMember.getMemberId());

    assertEquals(fakeMember.getMemberId(), response.getMemberId());
    assertFalse(fakeMember.isFirstLogIn());
    assertIterableEquals(List.of("관심주제1", "관심주제2"), response.getInterests());

  }


  @DisplayName("내 관심주제 설정 실패 - 존재하지 않는 태그")
  @Test
  void registerInterest_fail() {
    // Given
    Member member = MemberTestData.fakeMemberBuilder().build();


    MemberInterestDto.Request request = new MemberInterestDto.Request(List.of("관심주제1", "관심주제2"));

    when(tagRepository.findByName(any())).thenReturn(Optional.empty());

    // When
    TagException tagException = assertThrows(TagException.class,
        () -> memberService.registerInterest(member, request));

    // Then
    assertEquals(NOT_FOUND_TAG, tagException.getErrorCode());

    verify(memberTagRepository, times(1)).deleteAllByMember_MemberId(member.getMemberId());
    verify(memberTagRepository, never()).saveAll(any());

  }


  @DisplayName("나의 활동게시글 조회")
  @Test
  void getMyParticipatePosts() {
    //Given
    Member fakeMember = MemberTestData.fakeMemberBuilder().build();

    PostState postState = PostState.ONGOING;
    PostParticipateType postParticipateType = PostParticipateType.CHAT;
    PostOrder postOrder = PostOrder.POST_ORDER;
    Pageable pageable = Pageable.ofSize(10);

    List<MyParticipatePostDto> fakeMyParticipatePosts = creatFakeMyParticipatePosts();


    when(postRepository.getMyParticipatePostPages(
        eq(fakeMember.getMemberId()),
        eq(postParticipateType),
        eq(postState),
        eq(postOrder),
        eq(pageable)
    )).thenReturn(new PageImpl<>(fakeMyParticipatePosts, pageable, fakeMyParticipatePosts.size()));

    // When
    Page<MyParticipatePostDto> result = memberService.getMyParticipatePosts(
        fakeMember, postState, postParticipateType, postOrder, pageable);

    // Then
    assertEquals(fakeMyParticipatePosts.size(), result.getContent().size());

  }


  private List<MyParticipatePostDto> creatFakeMyParticipatePosts() {
    List<MyParticipatePostDto> fakeData = new ArrayList<>();

    MyParticipatePostDto post1 = MyParticipatePostDto.builder()
        .postId(1L)
        .writerNickname("작성자1")
        .title("제목1")
        .contents("내용1")
        .createdAt(LocalDateTime.now())
        .thumbnailUrl("이미지1")
        .voteCount(4)
        .chatCount(20)
        .build();

    MyParticipatePostDto post2 = MyParticipatePostDto.builder()
        .postId(2L)
        .writerNickname("작성자2")
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