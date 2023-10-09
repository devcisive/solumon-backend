package com.example.solumonbackend.post.model;

import com.example.solumonbackend.post.entity.Choice;
import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.model.PostDto.ImageDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.example.solumonbackend.post.model.PostDto.VoteDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.solumonbackend.post.model.PostDto.ChoiceDto;

public class PostAddDto {

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
    @Valid
    private VoteDto vote;
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
    private VoteDto vote;

    public static Response postToResponse(Post post, List<PostTag> tags,
                                          List<Choice> choices, List<Image> images) {
      return Response.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .writer(post.getMember().getNickname())
          .contents(post.getContents())
          .createdAt(post.getCreatedAt())

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
                  .build())
              .collect(Collectors.toList()))

          .vote(VoteDto.builder()
              .endAt(post.getEndAt())
              .choices(choices.stream()
                  .filter(Objects::nonNull)
                  .map(choice -> ChoiceDto.builder()
                      .choiceNum(choice.getChoiceNum())
                      .choiceText(choice.getChoiceText())
                      .build())
                  .collect(Collectors.toList()))
              .build())
          .build();
    }
  }

}
