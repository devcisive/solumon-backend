package com.example.solumonbackend.post.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PostGeneralDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {
    private long postId;
    private String title;
    private String preview;
    private String imageUrl;
    private int voteCount;
    private int chatCount;
  }

}
