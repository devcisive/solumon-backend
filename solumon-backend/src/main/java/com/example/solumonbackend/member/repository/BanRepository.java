package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.Ban;
import com.example.solumonbackend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {


  boolean existsByMember(Member member);


  // Ban 데이터 지우기 (나중에 스케줄러에 넣을것)
  // 호출 순서: releaseBan() -> removeBan()
  @Modifying
  @Transactional
  @Query(
      nativeQuery = true,
      value =
          "delete from ban b where b.member_member_id in"
              + "(select m.member_id  from member m where m.`role` = 'ROLE_GENERAL');"
  )
  void removeBan();
}
