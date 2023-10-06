package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

  boolean existsByPost_PostIdAndMember_MemberId(Long postId, Long memberId);

  void deleteAllByPost_PostId(Long postId);

  void deleteByPost_PostIdAndMember_MemberId(Long postId, Long memberId);

  int countByPost_PostId(Long postId);

}
