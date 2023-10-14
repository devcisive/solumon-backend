package com.example.solumonbackend.notify.repository;

import com.example.solumonbackend.notify.entity.Notify;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  private final Map<String, Notify> eventCache = new ConcurrentHashMap<>();

  @Override
  public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
    emitters.put(emitterId, sseEmitter);
    return sseEmitter;
  }

  @Override
  public void saveEventCache(String emitterId, Notify event) {
    eventCache.put(emitterId, event);
  }

  @Override
  public Map<String, SseEmitter> findAllEmitterStartWithByEmail(String email) {
    return emitters.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(email))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Map<String, Notify> findAllEventCacheStartWithByEmail(String email) {
    return eventCache.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(email))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public void deleteById(String emitterId) {
    emitters.remove(emitterId);
  }

  @Override
  public void deleteAllEmitterStartWithEmail(String email) {
    emitters.forEach(
        (key, emitter) -> {
          if (key.startsWith(email)) {
            emitters.remove(key);
          }
        }
    );
  }

  @Override
  public void deleteAllEventCacheStartWithEmail(String email) {
    eventCache.forEach(
        (key, emitter) -> {
          if (key.startsWith(email)) {
            eventCache.remove(key);
          }
        }
    );
  }

}
