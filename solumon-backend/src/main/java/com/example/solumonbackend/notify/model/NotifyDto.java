package com.example.solumonbackend.notify.model;

import com.example.solumonbackend.notify.entity.Notify;
import com.example.solumonbackend.notify.type.NotifyType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotifyDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {

    private Long memberId;
    private List<Notification> notifications;

    public static NotifyDto.Response notifyListToResponse(Long memberId, Page<Notify> notifyList) {
      return Response.builder()
          .memberId(memberId)
          .notifications(notifyList.stream()
              .map(Notification::notifyToNotification)
              .collect(Collectors.toList()))
          .build();
    }
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Notification {
    private Long notiId;
    private Long postId;
    private String postTitle;
    private boolean isRead;
    private NotifyType type;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime sentAt;


    public static NotifyDto.Notification notifyToNotification(Notify notify) {
      return Notification.builder()
          .notiId(notify.getNotiId())
          .postId(notify.getPostId())
          .postTitle(notify.getPostTitle())
          .isRead(notify.isRead())
          .type(notify.getType())
          .sentAt(LocalDateTime.now())
          .build();
    }
  }

}
