package com.example.solumonbackend.member.entity;

import com.example.solumonbackend.post.entity.Tag;
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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class MemberTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long memberTagId;

  @JoinColumn(name = "member_id")
  @ManyToOne
  private Member member;

  @JoinColumn(name = "tag_id")
  @ManyToOne
  private Tag tag;


  public MemberTag(Member member, Tag tag) {
    this.member = member;
    this.tag = tag;
  }

}
