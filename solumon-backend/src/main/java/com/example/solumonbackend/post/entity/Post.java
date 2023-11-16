package com.example.solumonbackend.post.entity;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.type.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long postId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "member_id")
  private Member member;

  private String title;

  private String contents;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime modifiedAt;

  private String thumbnailUrl;

  private LocalDateTime endAt;

  private int voteCount;

  private int chatCount;

  private PostStatus postStatus;

  public void setTitle(String title) {
    this.title = title;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public void setThumbnailUrl(String imageUrl) {
    this.thumbnailUrl = imageUrl;
  }

  public void setVoteCount(int voteCount) {
    this.voteCount = voteCount;
  }

  public void setChatCount(int chatCount) {
    this.chatCount = chatCount;
  }

  public void deletePost(){
    this.postStatus = PostStatus.DELETED;
  }
}
