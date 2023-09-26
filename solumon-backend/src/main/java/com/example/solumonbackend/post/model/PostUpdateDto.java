package com.example.solumonbackend.post.model;

import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.model.PostDto.ImageDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PostUpdateDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class Request {
    @NotBlank(message = "제목을 입력해주세요")
    private String title;
    @NotBlank(message = "상세 내용을 입력해주세요")
    private String contents;
    private List<TagDto> tags;
  }

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
    private List<TagDto> tags;
    private List<ImageDto> images;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("vote_count")
    private int voteCount;
    @JsonProperty("chat_count")
    private int chatCount;

    // TODO : chatCount 추가
    public static Response postToResponse(Post post, List<TagDto> tags, List<Image> images,
                                          int voteCount) {
      return Response.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .writer(post.getMember().getNickname())
          .contents(post.getContents())
          .tags(tags)
          .images(images.stream()
              .filter(Objects::nonNull)
              .map(image -> ImageDto.builder()
                  .image(image.getImageUrl())
                  .build())
              .collect(Collectors.toList()))
          .createdAt(post.getCreatedAt())
          .voteCount(voteCount)
          .build();
    }
  }

}
