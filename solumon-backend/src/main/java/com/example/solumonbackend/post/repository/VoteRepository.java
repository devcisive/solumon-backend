package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

  boolean existsByPostAndMember(Post post, Member member);

  void deleteAllByPost(Post post);

  void deleteByPostAndMember(Post post, Member member);

}
