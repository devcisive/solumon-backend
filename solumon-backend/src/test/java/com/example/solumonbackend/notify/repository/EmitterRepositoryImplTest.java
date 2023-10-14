package com.example.solumonbackend.notify.repository;

import com.example.solumonbackend.notify.entity.Notify;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class EmitterRepositoryImplTest {

  private EmitterRepository emitterRepository;

  private Long DEFAULT_TIMEOUT = 60L * 1000L * 60L;

  @BeforeEach
  void setup() {
    this.emitterRepository = new EmitterRepositoryImpl();
  }

  @Test
  @DisplayName("새로운 Emitter 추가 성공")
  void saveEmitter() throws Exception {
    // given
    String email = "zerobase@gmail.com";
    String emitterId = makeTimeIncludeEmail(email);
    SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

    //when
    SseEmitter savedEmitter = emitterRepository.save(emitterId, sseEmitter);

    // then
    Assertions.assertDoesNotThrow(() -> emitterRepository.save(emitterId, sseEmitter));
    Assertions.assertEquals(savedEmitter,
        emitterRepository.findAllEmitterStartWithByEmail(email).get(emitterId));
  }

  @DisplayName("특정 회원의 모든 Emitter 가져오기")
  @Test
  void findAllEmitterStartWithByMemberId() throws Exception {
    // given
    String email = "zerobase@gmail.com";
    String emitterId1 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId2 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId3 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId3, new SseEmitter(DEFAULT_TIMEOUT));

    //when
    Map<String, SseEmitter> allEmitterStartWithByEmail = emitterRepository.findAllEmitterStartWithByEmail(
        email);

    // then
    Assertions.assertDoesNotThrow(() -> emitterRepository.findAllEmitterStartWithByEmail(email));
    Assertions.assertEquals(3, allEmitterStartWithByEmail.size());
  }

  private static String makeTimeIncludeEmail(String email) {
    return email + "_" + System.currentTimeMillis();
  }

  @DisplayName("특정 회원의 모든 이벤트 캐시 가져오기")
  @Test
  void findAllEventCacheStartWithByEmail() throws Exception {
    // given
    String email = "zerobase@gmail.com";
    Notify notify1 = new Notify();
    String eventCacheId1 = makeTimeIncludeEmail(email);
    emitterRepository.saveEventCache(eventCacheId1, notify1);

    Thread.sleep(100);
    Notify notify2 = new Notify();
    String eventCacheId2 = makeTimeIncludeEmail(email);
    emitterRepository.saveEventCache(eventCacheId2, notify2);

    Thread.sleep(100);
    Notify notify3 = new Notify();
    String eventCacheId3 = makeTimeIncludeEmail(email);
    emitterRepository.saveEventCache(eventCacheId3, notify3);

    //when
    Map<String, Notify> allEventCacheStartWithByEmail = emitterRepository.findAllEventCacheStartWithByEmail(
        email);

    // then
    Assertions.assertDoesNotThrow(() -> emitterRepository.findAllEventCacheStartWithByEmail(email));
    Assertions.assertEquals(3, allEventCacheStartWithByEmail.size());
  }

  @DisplayName("특정 emitterId에 해당하는 emitter 삭제")
  @Test
  void deleteById() throws Exception {
    // given
    String email = "zerobase@gmail.com";

    String emitterId1 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId2 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId3 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId3, new SseEmitter(DEFAULT_TIMEOUT));

    //when
    emitterRepository.deleteById(emitterId2);

    // then
    Assertions.assertEquals(2, emitterRepository.findAllEmitterStartWithByEmail(email).size());
  }

  @DisplayName("특정 email에 해당하는 emitter 전체 삭제")
  @Test
  void deleteAllEmitterStartWithEmail() throws Exception {
    // given
    String email = "zerobase@gmail.com";

    String emitterId1 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId2 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId3 = makeTimeIncludeEmail(email);
    emitterRepository.save(emitterId3, new SseEmitter(DEFAULT_TIMEOUT));

    //when
    emitterRepository.deleteAllEmitterStartWithEmail(email);

    // then
    Assertions.assertEquals(0, emitterRepository.findAllEmitterStartWithByEmail(email).size());
  }

  @DisplayName("특정 email에 해당하는 event cache 전체 삭제")
  @Test
  void deleteAllEventCacheStartWithEmail() throws Exception {
    // given
    String email = "zerobase@gmail.com";
    Notify notify1 = new Notify();
    String eventCacheId1 = makeTimeIncludeEmail(email);
    emitterRepository.saveEventCache(eventCacheId1, notify1);

    Thread.sleep(100);
    Notify notify2 = new Notify();
    String eventCacheId2 = makeTimeIncludeEmail(email);
    emitterRepository.saveEventCache(eventCacheId2, notify2);

    Thread.sleep(100);
    Notify notify3 = new Notify();
    String eventCacheId3 = makeTimeIncludeEmail(email);
    emitterRepository.saveEventCache(eventCacheId3, notify3);

    //when
    emitterRepository.deleteAllEventCacheStartWithEmail(email);

    // then
    Assertions.assertEquals(0, emitterRepository.findAllEventCacheStartWithByEmail(email).size());
  }


}