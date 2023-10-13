package com.example.solumonbackend.notify.service;

import static com.example.solumonbackend.notify.model.NotifyDto.Response.notifyListToResponse;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.NotifyException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.notify.entity.Notify;
import com.example.solumonbackend.notify.model.NotifyDto;
import com.example.solumonbackend.notify.model.NotifyDto.Notification;
import com.example.solumonbackend.notify.repository.EmitterRepository;
import com.example.solumonbackend.notify.repository.NotifyRepository;
import com.example.solumonbackend.notify.type.NotifyType;
import com.example.solumonbackend.post.entity.Post;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotifyService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

  private final EmitterRepository emitterRepository;

  private final NotifyRepository notifyRepository;

  public SseEmitter subscribe(Member member, String lastEventId) {
    String email = member.getEmail();
    String emitterId = makeTimeIncludeEmail(email);
    SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

    // 시간이 만료된 경우에 대해 자동으로 레포지토리에서 삭제 처리해줄 수 있는 콜백을 등록
    emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
    emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
    emitter.onError((e) -> emitterRepository.deleteById(emitterId));

    // 503 에러를 방지하기 위한 더미 이벤트 전송
    // 연결이 되었다는 메세지 보내기
    String eventId = emitterId;
    sendDummyEvent(emitter, eventId, emitterId, "EventStream connect. [userEmail=" + email + "]");

    // 클라이언트가 미수신한 Event 목록이 존재할 경우,
    // 미수신한 Event 목록 전송하여 Event 유실을 예방
    if (hasLostData(lastEventId)) {
      sendLostData(lastEventId, email, emitterId, emitter);
    }

    return emitter;
  }

  public void send(Member receiver, Post post, NotifyType notifyType) {
    // notify 객체를 만들면서 sentAt을 일단 null 설정을 해줌
    Notify notification = notifyRepository.save(new Notify(receiver, post, notifyType));

    String receiverEmail = receiver.getEmail();
    String eventId = makeTimeIncludeEmail(receiverEmail);

    // MemberId로 emitter를 찾음
    Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByEmail(
        receiverEmail);

    emitters.forEach(
        (key, emitter) -> {
          emitterRepository.saveEventCache(key, notification);
          sendNotification(emitter, eventId, key, notification);
        }
    );
  }

  public NotifyDto.Response getNotifyList(Member member, int pageNum) {
    Page<Notify> allNotifyList = notifyRepository.findAllByMember_MemberIdAndSentAtIsNotNull(
        member.getMemberId(), PageRequest.of(pageNum - 1, 10));
    return notifyListToResponse(member.getMemberId(), allNotifyList);
  }

  public void deleteAllNotify(Member member) {
    notifyRepository.deleteAllByMember_MemberId(member.getMemberId());
  }

  public void changeReadStatus(long notiId) {
    Notify notify = notifyRepository.findById(notiId)
        .orElseThrow(() -> new NotifyException(ErrorCode.NOT_FOUND_NOTIFY));
    notify.setRead(true);
    notifyRepository.save(notify);
  }

  private String makeTimeIncludeEmail(String email) {
    return email + "_" + System.currentTimeMillis();
  }

  private void sendDummyEvent(SseEmitter emitter, String eventId, String emitterId, String message) {
    try {
      emitter.send(SseEmitter.event()
          .id(eventId)
          .name("notification")
          .data(message)
      );

    } catch (IOException exception) {
      emitterRepository.deleteById(emitterId);
      throw new NotifyException(ErrorCode.FAIL_SEND_NOTIFY);
    }
  }

  private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Notify data) {
    try {
      // notify -> notificationDto로 변환할때 sentAt 설정
      Notification notification = Notification.notifyToNotification(data);
      emitter.send(SseEmitter.event()
          .id(eventId)
          .name("notification")
          .data(notification)
      );

      // 보내는게 성공하면 sentAt을 실제 notify에 저장
      // 보내는게 실패했다면 앞단에서 exception이 발생해서 저장이 되지 않을 것임.
      data.setSentAt(notification.getSentAt());
      notifyRepository.save(data);

    } catch (IOException exception) {
      emitterRepository.deleteById(emitterId);
      throw new NotifyException(ErrorCode.FAIL_SEND_NOTIFY);
    }
  }

  private boolean hasLostData(String lastEventId) {
    return !lastEventId.isEmpty();
  }

  private void sendLostData(String lastEventId, String userEmail, String emitterId,
      SseEmitter emitter) {
    Map<String, Notify> eventCaches = emitterRepository.findAllEventCacheStartWithByEmail(
        userEmail);

    eventCaches.entrySet().stream()
        .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
        .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
  }


}
