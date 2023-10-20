package com.example.solumonbackend.notify.controller;

import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.notify.model.NotifyDto;
import com.example.solumonbackend.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/user/noti")
@RequiredArgsConstructor
public class NotifyController {
  private final NotifyService notifyService;

  // 재연결 할때
  @GetMapping(value = "/subscribe", produces = "text/event-stream")
  public SseEmitter subscribe(@AuthenticationPrincipal MemberDetail memberDetail,
      @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
    return notifyService.subscribe(memberDetail.getMember(), lastEventId);
  }

  @GetMapping
  public ResponseEntity<NotifyDto.Response> getNotifyList(@AuthenticationPrincipal MemberDetail memberDetail,
      @RequestParam(defaultValue = "1") int pageNum) {
    return ResponseEntity.ok(notifyService.getNotifyList(memberDetail.getMember(), pageNum));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteAllNotify(@AuthenticationPrincipal MemberDetail memberDetail) {
    notifyService.deleteAllNotify(memberDetail.getMember());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{notiId}")
  public ResponseEntity<Long> changeToRead(@PathVariable long notiId) {
    notifyService.changeToRead(notiId);
    return ResponseEntity.ok(notiId);
  }

}
