package com.example.solumonbackend.post.model;

import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.model.PostDto.ChoiceResultDto;
import com.example.solumonbackend.post.model.PostDto.ImageDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.example.solumonbackend.post.model.PostDto.VoteResultDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

public class PostDetailDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {
    // TODO : 채팅 추가
    @JsonProperty("post_id")
    private long postId;
    private String title;
    private String writer;
    private String contents;
    private List<TagDto> tags;
    private List<ImageDto> images;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    private VoteResultDto vote;
    @JsonProperty("vote_count")
    private int voteCount;
    @JsonProperty("chat_count")
    private int chatCount;

    private Slice<ChatMessageDto.Response> lastChatMessages;


    // TODO : 채팅, chatCount 추가
    public static PostDetailDto.Response postToResponse(Post post, List<PostTag> tags,
                                                        List<Image> images, VoteResultDto voteResultDto,
                                                         Slice<ChatMessageDto.Response> lastChatMessages) {
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

          .vote(voteResultDto)
          .voteCount(voteResultDto.getChoices().stream()
              .map(ChoiceResultDto::getChoiceCount)
              .mapToInt(Long::intValue)
              .sum())

          .chatCount(post.getChatCount())
          .lastChatMessages(lastChatMessages)

          .build();
    }
  }

}
