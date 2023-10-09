package com.example.solumonbackend.post.entity;

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

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommend {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long recommendId;

  private Long memberId;

  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  private Double score;
}
