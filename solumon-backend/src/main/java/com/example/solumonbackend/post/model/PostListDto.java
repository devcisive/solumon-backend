package com.example.solumonbackend.post.model;

import com.example.solumonbackend.global.elasticsearch.PostDocument;
import com.example.solumonbackend.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private List<String> tags;

    public Response(long postId, String title, String writer, String contents,
        String imageUrl, int voteCount, int chatCount, LocalDateTime createdAt) {
      this.postId = postId;
      this.title = title;
      this.writer = writer;
      this.contents = contents;
      this.imageUrl = imageUrl;
      this.voteCount = voteCount;
      this.chatCount = chatCount;
      this.createdAt = createdAt;
    }

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

    public static Response postDocumentToPostListResponse(PostDocument postDocument) {
      return Response.builder()
          .postId(postDocument.getId())
          .title(postDocument.getTitle())
          .writer(postDocument.getWriter())
          .contents(postDocument.getContent())
          .imageUrl(postDocument.getImageUrl())
          .voteCount(postDocument.getVoteCount())
          .chatCount(postDocument.getChatCount())
          .createdAt(LocalDateTime.parse(postDocument.getCreatedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
          .tags(postDocument.getTags())
          .build();
    }

  }
}
