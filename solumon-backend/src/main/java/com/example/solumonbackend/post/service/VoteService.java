package com.example.solumonbackend.post.service;

import com.example.solumonbackend.global.elasticsearch.PostSearchService;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Vote;
import com.example.solumonbackend.post.model.VoteAddDto;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.VoteRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

  private final VoteRepository voteRepository;
  private final PostRepository postRepository;
  private final PostSearchService postSearchService;

  @Transactional
  public VoteAddDto.Response createVote(Member member, long postId, int selectedNum) {
    Post post = getPost(postId);
    validateIsPostClosed(post);

    if (Objects.equals(post.getMember().getMemberId(), member.getMemberId())) {
      throw new PostException(ErrorCode.WRITER_CAN_NOT_VOTE);
    }

    if (voteRepository.existsByPost_PostIdAndMember_MemberId(postId, member.getMemberId())) {
      throw new PostException(ErrorCode.VOTE_ONLY_ONCE);
    }

    voteRepository.save(Vote.builder()
        .selectedNum(selectedNum)
        .member(member)
        .post(post)
        .build());

    // 게시글 투표수 업데이트
    post.setVoteCount(voteRepository.countByPost_PostId(postId));
    postRepository.save(post);
    postSearchService.updateVoteCount(post.getVoteCount(), post.getPostId());

    return VoteAddDto.Response.builder()
        .choices(voteRepository.getChoiceResults(postId))
        .build();
  }

  @Transactional
  public void deleteVote(Member member, long postId) {
    Post post = getPost(postId);
    validateIsPostClosed(post);

    if (!voteRepository.existsByPost_PostIdAndMember_MemberId(postId, member.getMemberId())) {
      throw new PostException(ErrorCode.ONLY_THE_PERSON_WHO_VOTED_CAN_CANCEL);
    }

    voteRepository.deleteByPost_PostIdAndMember_MemberId(postId, member.getMemberId());

    // 게시글 투표수 업데이트
    post.setVoteCount(voteRepository.countByPost_PostId(postId));
    postRepository.save(post);
    postSearchService.updateVoteCount(post.getVoteCount(), post.getPostId());
  }

  private Post getPost(long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new PostException(ErrorCode.NOT_FOUND_POST));
  }

  private void validateIsPostClosed(Post post) {
    if (post.getEndAt().isBefore(LocalDateTime.now())) {
      throw new PostException(ErrorCode.POST_IS_CLOSED);
    }
  }

}
