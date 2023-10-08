package com.example.solumonbackend.global.redis;

import com.example.solumonbackend.chat.model.ChatMemberInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class RedisService {

  private final RedisTemplate<String, ChatMemberInfo> chatMemberInfoRedisTemplate;


  // stomp connect 시에 생성된 세션 아이디 : 토큰으로부터 뽑은 멤버정보를 같이 레디스에 저장
  public void saveChatMemberInfo(String sessionId, ChatMemberInfo memberInfo) {

    HashOperations<String, Object, Object> hashOperations = chatMemberInfoRedisTemplate.opsForHash();
    Map<String, Object> chatMemberInfo = new HashMap<>();
    chatMemberInfo.put("memberId", memberInfo.getMemberId());
    chatMemberInfo.put("nickname", memberInfo.getNickname());
    hashOperations.putAll(sessionId, chatMemberInfo); // 세션아이디 : 멤버정보 형태로 저장
  }


  // 채팅메세지 보낼 때 저장될 chatMessage 엔티티에 들어갈 멤버 정보를 뽑기
  public ChatMemberInfo getChatMemberInfo(String sessionId) {

    HashOperations<String, Object, Object> hashOperations = chatMemberInfoRedisTemplate.opsForHash();
    Map<Object, Object> chatMemberInfoMap = hashOperations.entries(sessionId);

    Long memberId = null;
    String nickname = null;
    try {
      memberId = Long.valueOf(chatMemberInfoMap.get("memberId").toString());
      nickname = chatMemberInfoMap.get("nickname").toString();

    } catch (NullPointerException e) {
      // 처리를 어떻게 하는게 좋을지
      log.info("ChatMemberInfo is null" + e.getMessage());
    }

    return new ChatMemberInfo(memberId, nickname);
  }


  // 구독취소나 disconnect 할때 저장해놨던 멤버 정보 삭제
  public void deleteChatMemberInfo(String sessionId) {
    chatMemberInfoRedisTemplate.delete(sessionId);
    log.info("delete ChatMemberInfo: " + sessionId);
  }

}
