package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;
  private final QMember qMember = QMember.member;

  @Override
  public List<Member> findMembersToReleaseBannedStatus() {

    // bannedAt이 7일이 지난 멤버들
    return jpaQueryFactory.select(qMember).from(qMember)
        .where(qMember.bannedAt.loe(LocalDateTime.now().minusDays(7))).fetch();
  }
}
