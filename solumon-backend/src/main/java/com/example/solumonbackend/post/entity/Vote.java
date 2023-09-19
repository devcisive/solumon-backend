package com.example.solumonbackend.post.entity;

import com.example.solumonbackend.member.entity.Member;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Vote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long voteId;

  @ManyToOne
  private Post post;

  @ManyToOne
  private Member member;

  private int selectedNum;
}
