package com.example.solumonbackend.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Vote;
import com.example.solumonbackend.post.model.PostDto;
import com.example.solumonbackend.post.model.VoteAddDto;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.VoteRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

  @Mock
  private VoteRepository voteRepository;

  @Mock
  private PostRepository postRepository;

  @InjectMocks
  private VoteService voteService;

  @BeforeEach
  public void setUp() {
    postMember = Member.builder()
        .memberId(1L)
        .email("test@gmail.com")
        .nickname("별명")
        .role(MemberRole.GENERAL)
        .build();

    otherMember = Member.builder()
        .memberId(2L)
        .email("test2@gmail.com")
        .nickname("별명2")
        .role(MemberRole.GENERAL)
        .build();

    post = Post.builder()
        .postId(1L)
        .title("제목")
        .contents("내용")
        .member(postMember)
        .endAt(LocalDateTime.of(2023, 9, 28, 10, 0, 0)
            .plusDays(20))
        .build();
  }

  Member postMember;
  Member otherMember;
  Post post;

  @Test
  @DisplayName("투표하기 성공")
  void createVote_success() {
    //given
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1)
        .build();

    when(postRepository.findById(1L))
        .thenReturn(Optional.of(post));
    when(voteRepository.existsByPost_PostIdAndMember_MemberId(1L, 2L))
        .thenReturn(false);
    when(voteRepository.save(any(Vote.class)))
        .thenReturn(Vote.builder()
            .voteId(5L)
            .post(post)
            .member(otherMember)
            .selectedNum(1)
            .build());
    when(voteRepository.countByPost_PostId(1L))
        .thenReturn(5);
    when(voteRepository.getChoiceResults(1L))
        .thenReturn(List.of(
            PostDto.ChoiceResultDto.builder()
                .choiceNum(1)
                .choiceText("선택지1")
                .choiceCount(5L)
                .choicePercent(100L)
                .build(),
            PostDto.ChoiceResultDto.builder()
                .choiceNum(2)
                .choiceText("선택지2")
                .choiceCount(0L)
                .choicePercent(0L)
                .build()));

    //when
    VoteAddDto.Response response = voteService.createVote(otherMember, 1L, request);

    //then
    assertThat(response.getChoices().get(0).getChoiceCount()).isEqualTo(5L);
    assertThat(response.getChoices().get(1).getChoiceText()).isEqualTo("선택지2");

    verify(postRepository, times(1)).findById(1L);
    verify(voteRepository, times(1)).existsByPost_PostIdAndMember_MemberId(1L, 2L);
    verify(voteRepository, times(1)).save(any(Vote.class));
    verify(voteRepository, times(1)).countByPost_PostId(1L);
    verify(postRepository, times(1)).save(any(Post.class));
    verify(voteRepository, times(1)).getChoiceResults(1L);
  }

  @Test
  @DisplayName("투표하기 실패 - 존재하지 않는 게시글")
  void createVote_fail_notFoundPost() {
    //given
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1)
        .build();

    when(postRepository.findById(2L))
        .thenReturn(Optional.empty());

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.createVote(otherMember, 2L, request));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("투표하기 실패 - 투표가 마감된 글")
  void createVote_fail_postIsClosed() {
    //given
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1)
        .build();

    when(postRepository.findById(1L))
        .thenReturn(Optional.of(Post.builder()
            .postId(1L)
            .member(postMember)
            .endAt(LocalDateTime.now().minusDays(2))
            .build()));

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.createVote(otherMember, 1L, request));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_IS_CLOSED);
  }

  @Test
  @DisplayName("투표하기 실패 - 작성자가 투표")
  void createVote_fail_writerCanNotVote() {
    //given
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1)
        .build();

    when(postRepository.findById(1L))
        .thenReturn(Optional.of(post));

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.createVote(postMember, 1L, request));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRITER_CAN_NOT_VOTE);
  }

  @Test
  @DisplayName("투표하기 실패 - 투표는 한 번만 가능")
  void createVote_fail_voteOnlyOnce() {
    //given
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1)
        .build();

    when(postRepository.findById(1L))
        .thenReturn(Optional.of(post));
    when(voteRepository.existsByPost_PostIdAndMember_MemberId(1L, 2L))
        .thenReturn(true);

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.createVote(otherMember, 1L, request));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VOTE_ONLY_ONCE);
  }

  @Test
  @DisplayName("투표 취소 성공")
  void deleteVote_success() {
    //given
    when(postRepository.findById(1L))
        .thenReturn(Optional.of(post));
    when(voteRepository.existsByPost_PostIdAndMember_MemberId(1L, 2L))
        .thenReturn(true);
    when(voteRepository.countByPost_PostId(1L))
        .thenReturn(4);

    //when
    voteService.deleteVote(otherMember, 1L);

    //then
    verify(postRepository, times(1)).findById(1L);
    verify(voteRepository, times(1)).existsByPost_PostIdAndMember_MemberId(1L, 2L);
    verify(voteRepository, times(1)).deleteByPost_PostIdAndMember_MemberId(1L, 2L);
    verify(voteRepository, times(1)).countByPost_PostId(1L);
    verify(postRepository, times(1)).save(any(Post.class));
  }

  @Test
  @DisplayName("투표 취소 실패 - 존재하지 않는 게시글")
  void deleteVote_fail_notFoundPost() {
    //given
    when(postRepository.findById(2L))
        .thenReturn(Optional.empty());

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.deleteVote(otherMember, 2L));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("투표 취소 실패 - 투표가 마감된 글")
  void deleteVote_fail_postIsClosed() {
    //given
    when(postRepository.findById(1L))
        .thenReturn(Optional.of(Post.builder()
            .postId(1L)
            .member(postMember)
            .endAt(LocalDateTime.now().minusDays(2))
            .build()));

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.deleteVote(otherMember, 1L));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_IS_CLOSED);
  }

  @Test
  @DisplayName("투표 취소 실패 - 투표하지 않은 사람")
  void deleteVote_fail_onlyPersonWhoVoted() {
    //given
    when(postRepository.findById(1L))
        .thenReturn(Optional.of(post));
    when(voteRepository.existsByPost_PostIdAndMember_MemberId(1L, 2L))
        .thenReturn(false);

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.deleteVote(otherMember, 1L));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ONLY_THE_PERSON_WHO_VOTED_CAN_CANCEL);
  }

}