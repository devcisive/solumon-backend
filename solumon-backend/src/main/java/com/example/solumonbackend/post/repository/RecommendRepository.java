package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Recommend;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {
  List<Recommend> findAllByMemberId(Long memberId);

  List<Recommend> findAllByMemberIdAndPostModifiedAtIsAfter(Long memberId, LocalDateTime localDateTime);
  boolean existsByMemberId(Long memberId);
  void deleteAllByMemberId(Long memberId);
}
