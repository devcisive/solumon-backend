package com.example.solumonbackend.post.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
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
    @NotBlank(message = "선택지 번호를 입력해주세요")
    private int choiceNum;
    @NotBlank(message = "선택지 내용을 입력해주세요")
    private String choiceText;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ChoiceResultDto {
    private Integer choiceNum;
    private String choiceText;
    private Long choiceCount;
    private Long choicePercent;

    public ChoiceResultDto setChoicePercent(Long choicePercent) {
      this.choicePercent = choicePercent;
      return this;
    }
  }

}
