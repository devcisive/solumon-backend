package com.example.solumonbackend.post.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class PostTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long postTagId;

  @ManyToOne
  private Post post;

  @ManyToOne
  private Tag tag;
}
