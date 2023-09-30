package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MemberTestData {


  // 상황에 맞게 오버라이딩해서 사용
  public static Member.MemberBuilder fakeMemberBuilder() {
    return Member.builder()
        .memberId(1L)
        .kakaoId(1L)
        .email("example@naver.com")
        .nickname("nickname")
        .registeredAt(LocalDateTime.now())
        .role(MemberRole.GENERAL)
        .modifiedAt(null)
        .unregisteredAt(null)
        .isFirstLogIn(false);

    /*
    여기서 encode를 사용하면 검증에서
     verify(passwordEncoder, times(1)).encode(); 실제 횟수가 한번 더 증가해서
     비밀번호는 필요시 Test 클래스에서 필수로 초기화하게끔 하였음
    * */
   

  }

  public static List<MemberTag> createMemberTags(Member member, List<Tag> tags) {
    AtomicLong memberTagIdGenerator = new AtomicLong();

    return tags.stream().map(tag ->
        MemberTag.builder()
            .memberTagId(memberTagIdGenerator.incrementAndGet())
            .member(member)
            .tag(tag)
            .build()).collect(Collectors.toList());

  }
}

