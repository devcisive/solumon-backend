package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories
@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<Member> findByNickname(String nickName);

  boolean existsByNickname(String nickname);
}
