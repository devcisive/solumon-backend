package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.Report;
import com.example.solumonbackend.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {


  Optional<Report> findTopByMemberAndReporterIdOrderByReportedAtDesc(Member member, Long reporterId);

  int countByMember(Member member);

}
