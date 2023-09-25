package com.example.solumonbackend.post.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class PostDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TagDto {
    private String tag;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ImageDto {
    private String image;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VoteDto {
    private List<ChoiceDto> choices;
    private LocalDateTime endAt;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VoteResultDto {
    private boolean resultAccessStatus;
    private List<ChoiceResultDto> choices;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ChoiceDto {
    private int choiceNum;
    private String choiceText;
  }

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ChoiceResultDto {
    private int choiceNum;
    private String choiceText;
    private Long choiceCount;
    private int choicePercent;
  }

}
