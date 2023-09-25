package com.example.solumonbackend.post.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class MyParticipatePostDto {

  private Long postId;

  private String writerNickname;

  private String title;

  private String contents;

  private LocalDateTime createdAt;

  private String thumbnailUrl;

  private int voteCount;

  private int chatCount;

}
