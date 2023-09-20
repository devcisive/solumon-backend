package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTagRepository extends JpaRepository<MemberTag,Long> {

  List<MemberTag> findAllByMember(Member member);

  void deleteAllByMember(Member member);
}
