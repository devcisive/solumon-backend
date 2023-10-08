package com.example.solumonbackend.chat.entity;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Post;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.persistence.Entity;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long messageId;


  private Long postId;
  private Long memberId;
  private String nickname;

  private String contents;

  private LocalDateTime createdAt;
  private boolean isSent;



}
