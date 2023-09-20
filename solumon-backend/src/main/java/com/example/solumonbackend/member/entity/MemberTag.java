package com.example.solumonbackend.member.entity;

import com.example.solumonbackend.post.entity.Tag;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class MemberTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long memberTagId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  private Tag tag;


  public MemberTag(Member member, Tag tag){
    this.member = member;
    this.tag = tag;
  }

}
