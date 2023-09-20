package com.example.solumonbackend.member.service;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Member member = memberRepository.findByEmail(email).orElseThrow(() -> new MemberException(
        ErrorCode.NOT_FOUND_MEMBER));

    return MemberDetail.builder()
        .memberId(member.getMemberId())
        .email(member.getEmail())
        .password(member.getPassword())
        .role(member.getRole())
        .build();
  }
}
