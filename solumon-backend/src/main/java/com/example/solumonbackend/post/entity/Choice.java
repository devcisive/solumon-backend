package com.example.solumonbackend.post.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Choice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long choiceId;

  @ManyToOne
  private Post post;

  private int choiceNum;

  private String choiceText;
}
