package com.example.solumonbackend.post.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Choice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long choiceId;

  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  private int choiceNum;

  private String choiceText;
}
