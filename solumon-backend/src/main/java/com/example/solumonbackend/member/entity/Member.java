package com.example.solumonbackend.member.entity;

import com.example.solumonbackend.member.type.MemberRole;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long memberId;

  private Long kakaoId;

  @Column(unique = true)
  private String email;

  private String password;

  @Column(unique = true)
  private String nickname;

  @CreatedDate
  private LocalDateTime registeredAt;

  @Enumerated(EnumType.STRING)
  private MemberRole role;

  private int reportCount;

  @LastModifiedDate
  private LocalDateTime modifiedAt;

  private LocalDateTime unregisteredAt;

  private boolean isFirstLogIn;
}
