package com.example.solumonbackend.member.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ban {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long banId;

  private Long bannedBy;

  private LocalDateTime bannedAt;

  @ManyToOne
  private Member member;
}
