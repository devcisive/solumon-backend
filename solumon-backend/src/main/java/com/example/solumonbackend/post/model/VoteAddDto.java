package com.example.solumonbackend.post.model;

import com.example.solumonbackend.post.model.PostDto.ChoiceResultDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class VoteAddDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {
    private int selectedNum;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {
    private List<ChoiceResultDto> choices;
  }

}
