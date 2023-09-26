package com.example.solumonbackend.post.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class MyParticipatePostDto {

  private Long postId;

  private String writerNickname;

  private String title;

  private String contents;


  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  @JsonSerialize(using= LocalDateTimeSerializer.class)
  private LocalDateTime createdAt;

  private String thumbnailUrl;

  private int voteCount;

  private int chatCount;

}
