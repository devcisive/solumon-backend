package com.example.solumonbackend.chat.entity;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Post;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ChannelMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Member member;

  @ManyToOne
  private Post post;
}
