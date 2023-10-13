package com.example.solumonbackend.notify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.NotifyException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.notify.entity.Notify;
import com.example.solumonbackend.notify.model.NotifyDto;
import com.example.solumonbackend.notify.repository.EmitterRepository;
import com.example.solumonbackend.notify.repository.NotifyRepository;
import com.example.solumonbackend.notify.type.NotifyType;
import com.example.solumonbackend.post.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class NotifyServiceTest {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
  @Mock
  private EmitterRepository emitterRepository;
  @Mock
  private NotifyRepository notifyRepository;
  @InjectMocks
  private NotifyService notifyService;

  private static String makeTimeIncludeEmail(String email) {
    return email + "_" + System.currentTimeMillis();
  }

  @DisplayName("알림 구독 성공")
  @Test
  void subscribe() {
    // given
    Member member = Member.builder()
        .memberId(5L)
        .email("zerobase@gmail.com")
        .build();
    String lastEventId = "";
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

    when(emitterRepository.save(anyString(), any(SseEmitter.class)))
        .thenReturn(emitter);

    // when
    SseEmitter mockSubscribe = notifyService.subscribe(member, lastEventId);
    ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<SseEmitter> emitterCaptor = ArgumentCaptor.forClass(SseEmitter.class);

    // then
    verify(emitterRepository, times(1)).save(emitterIdCaptor.capture(), emitterCaptor.capture());

    assertNotNull(emitterCaptor);
    assertTrue(emitterIdCaptor.getValue().startsWith(member.getEmail()));
  }

  @DisplayName("알림 전송 성공")
  @Test
  void send() throws Exception {
    // given
    Member member = Member.builder()
        .memberId(5L)
        .email("zerobase@gmail.com")
        .build();

    Post post = Post.builder()
        .postId(6L)
        .title("남자친구 생일 선물").build();

    NotifyType notifyType = NotifyType.CLOSE_POST;
    Notify notify = new Notify(member, post, notifyType);

    Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    emitterMap.put(makeTimeIncludeEmail(member.getEmail()), new SseEmitter(DEFAULT_TIMEOUT));
    Thread.sleep(100);
    emitterMap.put(makeTimeIncludeEmail(member.getEmail()), new SseEmitter(DEFAULT_TIMEOUT));
    Thread.sleep(100);
    emitterMap.put(makeTimeIncludeEmail(member.getEmail()), new SseEmitter(DEFAULT_TIMEOUT));

    when(notifyRepository.save(any(Notify.class))).thenReturn(notify);
    when(emitterRepository.findAllEmitterStartWithByEmail(member.getEmail())).thenReturn(
        emitterMap);

    // when
    notifyService.send(member, post, notifyType);

    // then
    ArgumentCaptor<Notify> notifyArgumentCaptor = ArgumentCaptor.forClass(Notify.class);

    verify(notifyRepository, times(4)).save(notifyArgumentCaptor.capture());
    verify(emitterRepository, times(3)).saveEventCache(anyString(), notifyArgumentCaptor.capture());
    verify(emitterRepository, times(1)).findAllEmitterStartWithByEmail(member.getEmail());

    Notify notifyArgumentCaptorValue = notifyArgumentCaptor.getValue();

    assertEquals(notifyArgumentCaptorValue.getMember().getMemberId(), member.getMemberId());
    assertEquals(notifyArgumentCaptorValue.getMember().getEmail(), member.getEmail());
    assertEquals(notifyArgumentCaptorValue.getPostId(), post.getPostId());
    assertEquals(notifyArgumentCaptorValue.getPostTitle(), post.getTitle());
    assertEquals(notifyArgumentCaptorValue.getType(), notifyType);
    assertNotNull(notifyArgumentCaptorValue.getSentAt());
  }

  @DisplayName("읽음으로 상태 변환 성공")
  @Test
  void changeReadStatus() {
    // given
    when(notifyRepository.findById(1L))
        .thenReturn(Optional.of(Notify.builder()
            .isRead(false)
            .notiId(1L)
            .build()));
    // when
    notifyService.changeReadStatus(1L);

    ArgumentCaptor<Notify> notifyCaptor = ArgumentCaptor.forClass(Notify.class);

    // then
    verify(notifyRepository, times(1)).findById(1L);
    verify(notifyRepository, times(1)).save(notifyCaptor.capture());

    assertEquals(true, notifyCaptor.getValue().isRead());
  }

  @DisplayName("읽음으로 상태 변환 실패 - NOT_FOUND_NOTIFY")
  @Test
  void failed_changeReadStatus_NOT_FOUND_NOTIFY() {
    // given
    Long notiId = 3L;
    when(notifyRepository.findById(notiId)).thenReturn(Optional.empty());

    // when
    NotifyException notifyException = assertThrows(NotifyException.class,
        () -> notifyService.changeReadStatus(notiId));

    // then
    verify(notifyRepository, times(1)).findById(notiId);

    assertEquals(ErrorCode.NOT_FOUND_NOTIFY, notifyException.getErrorCode());
  }

  @DisplayName("알림 리스트 조회 성공")
  @Test
  void getNotifyList() {
    // given
    Member member = Member.builder()
        .memberId(5L)
        .email("zerobase@gmail.com")
        .build();

    int pageNum = 1;

    when(notifyRepository.findAllByMember_MemberIdAndSentAtIsNotNull(eq(member.getMemberId()),
        any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(
            Notify.builder()
                .notiId(1L)
                .postTitle("남자친구 선물")
                .sentAt(LocalDateTime.now())
                .isRead(false)
            .build(),
            Notify.builder()
                .notiId(2L)
                .postTitle("여자친구 선물")
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build()), PageRequest.of(0, 10), 2));

    // when
    NotifyDto.Response response = notifyService.getNotifyList(member, pageNum);
    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);

    // then
    verify(notifyRepository, times(1))
        .findAllByMember_MemberIdAndSentAtIsNotNull(eq(member.getMemberId()), pageRequestArgumentCaptor.capture());

    assertEquals(5L, response.getMemberId());
    assertEquals("남자친구 선물", response.getNotifications().get(0).getPostTitle());
    assertEquals(2L, response.getNotifications().get(1).getNotiId());
  }

}