package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.MemberTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberTagRepository extends JpaRepository<MemberTag, Long> {

  List<MemberTag> findAllByMember_MemberId(Long memberId);
  void deleteAllByMember_MemberId(Long memberId);

  boolean existsByMember_MemberId(Long memberId);

}
