package com.example.solumonbackend.notify.entity;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.notify.type.NotifyType;
import com.example.solumonbackend.post.entity.Post;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Notify {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notiId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "member_id")
  private Member member;
  private String postTitle;
  private Long postId;
  private boolean isRead;
  private LocalDateTime sentAt;
  private NotifyType type;

  public Notify(Member member, Post post, NotifyType notifyType) {
    this.member = member;
    this.postTitle = post.getTitle();
    this.postId = post.getPostId();
    this.isRead = false;
    this.type = notifyType;
  }

  public void setSentAt(LocalDateTime sentAt) {
    this.sentAt = sentAt;
  }

  public void setRead(boolean isRead) {
    this.isRead = isRead;
  }

}
