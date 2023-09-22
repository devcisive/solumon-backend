package com.example.solumonbackend.post.model;

import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.model.PostDto.ImageDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.example.solumonbackend.post.model.PostDto.VoteDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class PostAddDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {
    private String title;
    private String contents;
    private List<TagDto> tags;
    private List<ImageDto> images;
    private VoteDto vote;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {
    private long postId;
    private String title;
    private String writer;
    private String contents;
    private List<TagDto> tags;
    private List<ImageDto> images;
    private LocalDateTime createdAt;
    private VoteDto vote;

    // TODO : 이미지 추가
    public static Response postToResponse(Post post, List<TagDto> tags, VoteDto vote) {
      return Response.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .writer(post.getMember().getNickname())
          .contents(post.getContents())
          .tags(tags)
//          .images()
          .createdAt(post.getCreatedAt())
          .vote(vote)
          .build();
    }
  }

}
