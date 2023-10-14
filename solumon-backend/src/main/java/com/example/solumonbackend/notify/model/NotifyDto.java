package com.example.solumonbackend.notify.model;

import static com.example.solumonbackend.notify.model.NotifyDto.Notification.notifyToNotification;

import com.example.solumonbackend.notify.entity.Notify;
import com.example.solumonbackend.notify.type.NotifyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

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
    @JsonProperty("noti_id")
    private Long notiId;
    @JsonProperty("post_id")
    private Long postId;
    @JsonProperty("post_title")
    private String postTitle;
    @JsonProperty("is_read")
    private boolean isRead;
    private NotifyType type;
    @JsonProperty("sent_at")
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
