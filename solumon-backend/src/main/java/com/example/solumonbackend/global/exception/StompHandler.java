package com.example.solumonbackend.global.exception;

import com.example.solumonbackend.chat.model.ChatMemberInfo;
import com.example.solumonbackend.chat.service.RedisChatService;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;
  private final RedisChatService redisChatService;

  // Stomp 메세지를 전송하기 전 호출되는 메소드
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    log.debug("Stomp handler 실행");
    // StompHeaderAccessor.wrap 으로 message 를 감싸면 STOMP 의 헤더에 직접 접근가능
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    String accessToken = "";

    // 처음 websocket 연결시에만 헤더의 jwt token 유효성 검증
    // 로직은 JwtAuthenticationFilter 의 doFilterInternal 와 동일
    // 이 로직에서 GlobalExceptionHandler 에 넣어둔 예외가 발생해도 처리가 안돼서 설정에 별도의 핸들러를 넣어서 사용 (소켓에서 터진 에러)
    if (StompCommand.CONNECT == accessor.getCommand()) {
      log.info("[websocket] CONNECT");
      accessToken = accessor.getFirstNativeHeader("X-AUTH-TOKEN");

      // 제대로 됐을 때
      if (accessToken != null & jwtTokenProvider.validateTokenExpiration(accessToken)) {
        log.debug("[websocket] 토큰 유효 검증 성공");

        RefreshToken byAccessToken = refreshTokenRedisRepository.findByAccessToken(accessToken)
            .orElseThrow(() -> new CustomSecurityException(ErrorCode.NOT_FOUND_TOKEN_SET));

        // 로그아웃 처리된 액서스 토큰이 아닌지 검증, 아닐 시에만 authentication 부여
        // 로그아웃 처리된 액서스 토큰일 경우 authentication 부여하지 않고 401 error
        if (!"logout".equals(byAccessToken.getRefreshToken())) {
          Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } else if (accessToken != null & !jwtTokenProvider.validateTokenExpiration(accessToken)) {
        // 액세스 토큰으로 레디스에서 리프레쉬 토큰 가져오기
        RefreshToken byAccessToken = refreshTokenRedisRepository.findByAccessToken(accessToken)
            .orElseThrow(() -> new CustomSecurityException(ErrorCode.NOT_FOUND_TOKEN_SET));
        // 1. 리프레쉬도 만료됐다면 -> 다시 로그인 하도록 함
        if (!jwtTokenProvider.validateTokenExpiration(byAccessToken.getRefreshToken())) {
          throw new CustomSecurityException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 얘네가 정상이라면? 다시 AccessToken만들어서 기존 RefreshToken이랑 저장한 후 accessToken만 가져오기
        accessToken = jwtTokenProvider.reIssue(byAccessToken.getAccessToken());
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      // 토큰 유효성 검사를 통과하면 그 안에서 유저 정보를 뽑아오고 저장한다.(연결을 끊기전까지 사용)
      Member member = ((MemberDetail) jwtTokenProvider.getAuthentication(accessToken).getPrincipal()).getMember();

      boolean banChatting = false;
      if(member.getBannedAt() != null){
        banChatting = true;
      }

      redisChatService.saveChatMemberInfo(accessor.getSessionId(),
          new ChatMemberInfo(member.getMemberId(), member.getNickname(), banChatting));
    }

    // 웹소켓 연결 끊을 때 레디스에 저장해뒀던 유저정보 삭제
    if (StompCommand.DISCONNECT == accessor.getCommand()) {
      log.info(accessor.getSessionId() + ": DISCONNECT");
      redisChatService.deleteChatMemberInfo(accessor.getSessionId());
    }

    return message;
  }

}
