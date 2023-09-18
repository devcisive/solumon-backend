package com.example.solumonbackend.member.entity;

import com.example.solumonbackend.post.entity.Tag;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class MemberTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long memberTagId;

  @ManyToOne
  private Member member;

  @ManyToOne
  private Tag tag;
}
