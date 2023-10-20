package com.example.solumonbackend.post.model;

import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.model.PostDto.ImageDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
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
  public static class Request {
    @NotBlank(message = "제목을 입력해주세요")
    private String title;
    @NotBlank(message = "상세 내용을 입력해주세요")
    private String contents;
    private List<TagDto> tags;
    private List<ImageDto> images;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class Response {
    private long postId;
    private String title;
    private String nickname;
    private String contents;
    private List<TagDto> tags;
    private List<ImageDto> images;
    private LocalDateTime createdAt;
    private int voteCount;
    private int chatCount;

    public static Response postToResponse(Post post, List<PostTag> tags, List<Image> images) {
      return Response.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .nickname(post.getMember().getNickname())
          .contents(post.getContents())

          .tags(tags.stream()
              .filter(Objects::nonNull)
              .map(tag -> TagDto.builder()
                  .tag(tag.getTag().getName())
                  .build())
              .collect(Collectors.toList()))

          .images(images.stream()
              .filter(Objects::nonNull)
              .map(image -> ImageDto.builder()
                  .image(image.getImageUrl())
                  .index(images.indexOf(image) + 1)
                  .representative(Objects.equals(image.getImageUrl(), post.getThumbnailUrl()))
                  .build())
              .collect(Collectors.toList()))

          .createdAt(post.getCreatedAt())
          .voteCount(post.getVoteCount())
          .chatCount(post.getChatCount())
          .build();
    }
  }

}
