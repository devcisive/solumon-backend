package com.example.solumonbackend.post.model;

import com.example.solumonbackend.global.elasticsearch.PostDocument;
import com.example.solumonbackend.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostListDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {
    private long postId;
    private String title;
    private String nickname;
    private String contents;
    private String imageUrl;
    private int voteCount;
    private int chatCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;
    private List<String> tags;

    public Response(long postId, String title, String nickname, String contents,
                    String imageUrl, int voteCount, int chatCount, LocalDateTime createdAt) {
      this.postId = postId;
      this.title = title;
      this.nickname = nickname;
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
          .nickname(post.getMember().getNickname())
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
          .nickname(postDocument.getNickname())
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

