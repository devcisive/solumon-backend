package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Recommend;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {
  List<Recommend> findAllByMemberId(Long memberId);
  boolean existsByMemberId(Long memberId);
}
