package com.example.solumonbackend.chat.repository;

import com.example.solumonbackend.chat.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories
@Repository
public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {

  boolean existsByPostPostIdAndMemberMemberId(Long postId, Long memberId);

  int countByPost_PostId(Long postId);
}
