package com.example.solumonbackend.member.entity;

import com.example.solumonbackend.member.type.ReportSubject;
import com.example.solumonbackend.member.type.ReportType;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Report {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long reportId;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  private Long reporterId;

  private Long postId;

  private ReportSubject reportSubject;

  private String reportTargetMessage;

  @Enumerated(value = EnumType.STRING)
  private ReportType reportType;

  private String reportExplanation;

  private LocalDateTime reportedAt;

}

