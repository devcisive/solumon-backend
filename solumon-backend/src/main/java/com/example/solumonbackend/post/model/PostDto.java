package com.example.solumonbackend.post.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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
    private int index;
    private boolean representative;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VoteDto {
    private List<ChoiceDto> choices;
    @JsonProperty("end_at")
    private LocalDateTime endAt;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VoteResultDto {
    @JsonProperty("result_access_status")
    private boolean resultAccessStatus;
    private List<ChoiceResultDto> choices;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ChoiceDto {
    @JsonProperty("choice_num")
    @NotBlank(message = "선택지 번호를 입력해주세요")
    private int choiceNum;
    @JsonProperty("choice_text")
    @NotBlank(message = "선택지 내용을 입력해주세요")
    private String choiceText;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ChoiceResultDto {
    @JsonProperty("choice_num")
    private Integer choiceNum;
    @JsonProperty("choice_text")
    private String choiceText;
    @JsonProperty("choice_count")
    private Long choiceCount;
    @JsonProperty("choice_percent")
    private Long choicePercent;

    public ChoiceResultDto setChoicePercent(Long choicePercent) {
      this.choicePercent = choicePercent;
      return this;
    }
  }

}
