package com.example.solumonbackend.post.model;

import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.model.PostDto.ImageDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.example.solumonbackend.post.model.PostDto.VoteResultDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostDetailDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {
    // TODO : 채팅 추가
    private long postId;
    private String title;
    private String writer;
    private String contents;
    private List<TagDto> tags;
    private List<ImageDto> images;
    private LocalDateTime createdAt;
    private VoteResultDto vote;
    private int voteCount;
    private int chatCount;

    // TODO : 채팅, chatCount 추가
    public static PostDetailDto.Response postToResponse(Post post, List<PostTag> tags,
                                                        List<Image> images, VoteResultDto voteResultDto) {
      return PostDetailDto.Response.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .writer(post.getMember().getNickname())
          .contents(post.getContents())

          .tags(tags.stream()
              .map(tag -> TagDto.builder()
                  .tag(tag.getTag().getName())
                  .build())
              .collect(Collectors.toList()))

          .images(images.stream()
              .map(image -> ImageDto.builder()
                  .image(image.getImageUrl())
                  .build())
              .collect(Collectors.toList()))

          .vote(voteResultDto)
          .voteCount(voteResultDto.getChoices().stream()
              .map(PostDto.ChoiceResultDto::getChoiceCount)
              .mapToInt(Long::intValue)
              .sum())
          .build();
    }
  }

}
