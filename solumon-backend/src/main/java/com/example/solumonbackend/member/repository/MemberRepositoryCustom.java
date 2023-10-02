package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.Member;
import java.util.List;

public interface MemberRepositoryCustom {

  List<Member> findMembersToReleaseBannedStatus();

}
