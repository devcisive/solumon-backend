package com.example.solumonbackend.chat.service;

import com.example.solumonbackend.chat.model.ChatMemberInfo;
import com.example.solumonbackend.global.exception.ChatException;
import com.example.solumonbackend.global.exception.ErrorCode;
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
public class RedisChatService {

  private final RedisTemplate<String, ChatMemberInfo> redisChatMemberTemplate;


  // stomp connect 시에 생성된 세션 아이디 : 토큰으로부터 뽑은 멤버정보를 같이 레디스에 저장
  public void saveChatMemberInfo(String sessionId, ChatMemberInfo memberInfo) {

    HashOperations<String, Object, Object> hashOperations = redisChatMemberTemplate.opsForHash();
    Map<String, Object> chatMemberInfo = new HashMap<>();
    chatMemberInfo.put("memberId", memberInfo.getMemberId());
    chatMemberInfo.put("nickname", memberInfo.getNickname());
    chatMemberInfo.put("banChatting",memberInfo.isBanChatting());
    hashOperations.putAll(sessionId, chatMemberInfo); // 세션아이디 : 멤버정보 형태로 저장
  }


  // 채팅메세지 보낼 때 사용 될 멤버 정보를 뽑기
  public ChatMemberInfo getChatMemberInfo(String sessionId) {

    HashOperations<String, Object, Object> hashOperations = redisChatMemberTemplate.opsForHash();
    Map<Object, Object> chatMemberInfoMap = hashOperations.entries(sessionId);

    Long memberId = null;
    String nickname = null;
    Boolean banChatting = null;

    try {
      memberId = Long.valueOf(chatMemberInfoMap.get("memberId").toString());
      nickname = String.valueOf(chatMemberInfoMap.get("nickname"));
      banChatting = Boolean.parseBoolean(chatMemberInfoMap.get("banChatting").toString());

    } catch (RuntimeException e) {
      throw new ChatException(ErrorCode.CHAT_MEMBER_INFO_RETRIEVAL_ERROR);
    }

    return new ChatMemberInfo(memberId, nickname, banChatting);
  }


  // disconnect 할때 저장해놨던 멤버 정보 삭제
  public void deleteChatMemberInfo(String sessionId) {
    redisChatMemberTemplate.delete(sessionId);
    log.info("delete ChatMemberInfo: " + sessionId);
  }

}
