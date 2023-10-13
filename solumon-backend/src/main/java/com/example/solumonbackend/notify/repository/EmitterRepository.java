package com.example.solumonbackend.notify.repository;

import com.example.solumonbackend.notify.entity.Notify;
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {

  SseEmitter save(String emitterId, SseEmitter sseEmitter);

  void saveEventCache(String emitterId, Notify event);

  Map<String, SseEmitter> findAllEmitterStartWithByEmail(String memberId);

  Map<String, Notify> findAllEventCacheStartWithByEmail(String memberId);

  void deleteById(String emitterId);

  void deleteAllEmitterStartWithEmail(String memberId);

  void deleteAllEventCacheStartWithEmail(String memberId);

}
