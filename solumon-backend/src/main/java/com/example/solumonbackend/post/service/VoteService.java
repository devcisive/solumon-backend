package com.example.solumonbackend.post.service;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Vote;
import com.example.solumonbackend.post.model.VoteAddDto;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.VoteCustomRepository;
import com.example.solumonbackend.post.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoteService {

  private final VoteRepository voteRepository;
  private final VoteCustomRepository voteCustomRepository;
  private final PostRepository postRepository;


  // TODO : exception 수정
  public VoteAddDto.Response createVote(Member member, long postId, VoteAddDto.Request request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

    if (post.getEndAt().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("게시글이 마감되어 투표가 불가능합니다.");
    }

    if (voteRepository.existsByPostAndMember(post, member)) {
      throw new RuntimeException("투표는 한번만 가능합니다.");
    }

    voteRepository.save(Vote.builder()
        .selectedNum(request.getSelectedNum())
        .member(member)
        .post(post)
        .build());

    return VoteAddDto.Response.builder()
        .choices(voteCustomRepository.getChoiceResults(post))
        .build();
  }

  // TODO : exception 수정
  public void deleteVote(Member member, long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

    if (post.getEndAt().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("게시글이 마감되어 투표 취소가 불가능합니다.");
    }

    if (!voteRepository.existsByPostAndMember(post, member)) {
      throw new RuntimeException("투표를 한 사람만 취소 가능합니다.");
    }

    voteRepository.deleteByPostAndMember(post, member);
  }
}
