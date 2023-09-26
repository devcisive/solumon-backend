package com.example.solumonbackend.post.service;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Vote;
import com.example.solumonbackend.post.model.PostDto;
import com.example.solumonbackend.post.model.VoteAddDto;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.VoteCustomRepository;
import com.example.solumonbackend.post.repository.VoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

  @Mock
  private VoteRepository voteRepository;

  @Mock
  private VoteCustomRepository voteCustomRepository;

  @Mock
  private PostRepository postRepository;

  @InjectMocks
  private VoteService voteService;

  @Test
  @DisplayName("투표하기")
  void createVote() {
    //given
    Member member2 = Member.builder().memberId(2L).build();
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member())));
    when(voteRepository.existsByPost_PostIdAndMember_MemberId(anyLong(), anyLong()))
        .thenReturn(false);
    when(voteRepository.save(any(Vote.class)))
        .thenReturn(Vote.builder()
            .post(post(member()))
            .member(member2)
            .selectedNum(1)
            .build());
    when(voteCustomRepository.getChoiceResults(anyLong()))
        .thenReturn(List.of(PostDto.ChoiceResultDto.builder()
                .choiceNum(1)
                .choiceText("선택지1")
                .choiceCount(5L)
                .choicePercent(100.0)
                .build(),
            PostDto.ChoiceResultDto.builder()
                .choiceNum(2)
                .choiceText("선택지2")
                .choiceCount(0L)
                .choicePercent(0.0)
                .build()));

    //when
    VoteAddDto.Response response = voteService.createVote(member2, 1L, request);

    //then
    assertThat(response.getChoices().get(0).getChoiceCount()).isEqualTo(5L);
    assertThat(response.getChoices().get(1).getChoiceText()).isEqualTo("선택지2");

    verify(postRepository, times(1)).findById(anyLong());
    verify(voteRepository, times(1)).existsByPost_PostIdAndMember_MemberId(anyLong(), anyLong());
    verify(voteRepository, times(1)).save(any(Vote.class));
    verify(voteCustomRepository, times(1)).getChoiceResults(anyLong());
  }

  @Test
  @DisplayName("투표하기 실패 - 존재하지 않는 게시글")
  void createVote_fail_notFoundPost() {
    //given
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.createVote(member(), 1L, request));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("투표하기 실패 - 투표가 마감된 글")
  void createVote_fail_postIsClosed() {
    //given
    Member member2 = Member.builder().memberId(2L).build();
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(Post.builder()
            .postId(1L)
            .member(member())
            .endAt(LocalDateTime.now().minusDays(2))
            .build()));

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.createVote(member2, 1L, request));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_IS_CLOSED);
  }

  @Test
  @DisplayName("투표하기 실패 - 작성자가 투표")
  void createVote_fail_writerCanNotVote() {
    //given
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member())));

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.createVote(member(), 1L, request));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRITER_CAN_NOT_VOTE);
  }

  @Test
  @DisplayName("투표하기 실패 - 투표는 한 번만 가능")
  void createVote_fail_voteOnlyOnce() {
    //given
    Member member2 = Member.builder().memberId(2L).build();
    VoteAddDto.Request request = VoteAddDto.Request.builder()
        .selectedNum(1).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member())));
    when(voteRepository.existsByPost_PostIdAndMember_MemberId(anyLong(), anyLong()))
        .thenReturn(true);

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.createVote(member2, 1L, request));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VOTE_ONLY_ONCE);
  }

  @Test
  @DisplayName("투표 취소")
  void deleteVote() {
    //given
    Member member2 = Member.builder().memberId(2L).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member())));
    when(voteRepository.existsByPost_PostIdAndMember_MemberId(anyLong(), anyLong()))
        .thenReturn(true);

    //when
    voteService.deleteVote(member2, 1L);

    //then
    verify(postRepository, times(1)).findById(anyLong());
    verify(voteRepository, times(1)).existsByPost_PostIdAndMember_MemberId(anyLong(), anyLong());
    verify(voteRepository, times(1)).deleteByPost_PostIdAndMember_MemberId(anyLong(), anyLong());
  }

  @Test
  @DisplayName("투표 취소 실패 - 존재하지 않는 게시글")
  void deleteVote_fail_notFoundPost() {
    //given
    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.deleteVote(member(), 1L));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("투표 취소 실패 - 투표가 마감된 글")
  void deleteVote_fail_postIsClosed() {
    //given
    Member member2 = Member.builder().memberId(2L).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(Post.builder()
            .postId(1L)
            .member(member())
            .endAt(LocalDateTime.now().minusDays(2))
            .build()));

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.deleteVote(member2, 1L));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_IS_CLOSED);
  }

  @Test
  @DisplayName("투표 취소 실패 - 투표하지 않은 사람")
  void deleteVote_fail_onlyPersonWhoVoted() {
    //given
    Member member2 = Member.builder().memberId(2L).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member())));
    when(voteRepository.existsByPost_PostIdAndMember_MemberId(anyLong(), anyLong()))
        .thenReturn(false);

    //when
    PostException exception = assertThrows(PostException.class,
        () -> voteService.deleteVote(member2, 1L));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ONLY_THE_PERSON_WHO_VOTED_CAN_CANCEL);
  }

  private Member member() {
    return Member.builder()
        .memberId(1L)
        .email("test@gmail.com")
        .nickname("별명")
        .role(MemberRole.GENERAL)
        .build();
  }

  private Post post(Member member) {
    return Post.builder()
        .postId(1L)
        .title("제목")
        .contents("내용")
        .member(member)
        .endAt(LocalDateTime.now().plusDays(3))
        .build();
  }
}