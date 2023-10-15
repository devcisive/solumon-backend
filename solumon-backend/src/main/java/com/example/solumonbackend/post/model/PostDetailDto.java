package com.example.solumonbackend.post.model;

import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.model.PostDto.ImageDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.example.solumonbackend.post.model.PostDto.VoteResultDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostDetailDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {

    private long postId;
    private String title;
    private String nickname;
    private String contents;
    private List<TagDto> tags;
    private List<ImageDto> images;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endAt;
    private VoteResultDto vote;
    private int voteCount;
    private int chatCount;

    private Slice<ChatMessageDto.Response> lastChatMessages;


    public static PostDetailDto.Response postToResponse(Post post, List<PostTag> tags,
                                                        List<Image> images, VoteResultDto voteResultDto,
                                                         Slice<ChatMessageDto.Response> lastChatMessages) {
      return Response.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .nickname(post.getMember().getNickname())
          .contents(post.getContents())
          .createdAt(post.getCreatedAt())
          .endAt(post.getEndAt())

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

          .vote(voteResultDto)
          .voteCount(voteResultDto.getChoices().stream()
              .map(PostDto.ChoiceResultDto::getChoiceCount)
              .mapToInt(Long::intValue)
              .sum())

          .chatCount(post.getChatCount())
          .lastChatMessages(lastChatMessages)

          .build();
    }
  }

}
