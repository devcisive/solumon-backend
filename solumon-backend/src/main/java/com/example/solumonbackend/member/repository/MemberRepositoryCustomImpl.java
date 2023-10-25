package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.QMember;
import com.example.solumonbackend.member.type.MemberRole;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;


  @Override
  public List<Member> findMembersToReleaseBannedStatus() {

    QMember qMember = QMember.member;

    // bannedAt이 7일이 지난 멤버들 (영구정지상태는 제외)
    return jpaQueryFactory.select(qMember).from(qMember)
        .where(qMember.bannedAt.loe(LocalDateTime.now().minusDays(7)))
        .where(qMember.role.eq(MemberRole.BANNED))
        .fetch();
  }
}
