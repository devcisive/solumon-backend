package com.example.solumonbackend.member.repository;

import com.example.solumonbackend.member.entity.Report;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {


  Optional<Report> findTopByMemberMemberIdAndReporterIdOrderByReportedAtDesc(Long reportedId,
      Long reporterId);

  int countByMember_MemberId(Long memberId);

}
