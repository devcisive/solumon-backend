package com.example.solumonbackend.post.model;

import com.example.solumonbackend.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PostListDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {

    @JsonProperty("post_id")
    private long postId;
    private String title;
    private String writer;
    private String contents;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("vote_count")
    private int voteCount;
    @JsonProperty("chat_count")
    private int chatCount;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static Response postToPostListResponse(Post post) {
      return Response.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .writer(post.getMember().getNickname())
          .contents(post.getContents())
          .imageUrl(post.getThumbnailUrl())
          .voteCount(post.getVoteCount())
          .chatCount(post.getChatCount())
          .createdAt(post.getCreatedAt())
          .build();
    }
  }
}