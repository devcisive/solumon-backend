package com.example.solumonbackend.notify.repository;

import com.example.solumonbackend.notify.entity.Notify;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotifyRepository extends JpaRepository<Notify, Long> {

  Page<Notify> findAllByMember_MemberIdAndSentAtIsNotNull(Long memberId, Pageable pageable);

  void deleteAllByMember_MemberId(Long memberId);

}
