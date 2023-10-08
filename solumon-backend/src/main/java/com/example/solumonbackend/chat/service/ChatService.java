package com.example.solumonbackend.chat.service;

import com.example.solumonbackend.chat.entity.ChannelMember;
import com.example.solumonbackend.chat.entity.ChatMessage;
import com.example.solumonbackend.chat.model.ChatMemberInfo;
import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.chat.repository.ChannelMemberRepository;
import com.example.solumonbackend.chat.repository.ChatMessageRepository;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.repository.PostRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {


  /* SimpMessagingTemplate)
   @EnableWebSocketMessageBroker를 통해서 등록되는 bean이다. 특정 Broker로 메시지를 전달한다.
   메시지를 클라이언트로 보내거나, 클라이언트로부터 메시지를 수신하는 데 사용되는 템플릿
   이 템플릿을 사용하여 채팅 메시지를 전송하거나 브로드캐스트한다.
   */
  private final SimpMessagingTemplate template;
  private final ChatMessageRepository chatMessageRepository;
  private final ChannelMemberRepository channelMemberRepository;
  private final PostRepository postRepository;
  private final MemberRepository memberRepository;


  @Transactional
  public void sendAndSaveChatMessage(Long postId, ChatMessageDto.Request request,
      ChatMemberInfo chatMemberInfo) {

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("없는 게시물")); // pull 전이라 임시

    if (post.getEndAt().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("마감이 된 게시글임"); // pull 전이라 임시
    }

    // 해당 게시물에 채팅한 이력이 없다면 channelMember 생성 (없을때만 member 찾아오기)
    if (!channelMemberRepository.existsByPostPostIdAndMemberMemberId(postId,
        chatMemberInfo.getMemberId())) {

      Member member = memberRepository.findById(chatMemberInfo.getMemberId())
          .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

      channelMemberRepository.save(ChannelMember.builder().post(post).member(member).build());
    }

    template.convertAndSend("/sub/chat/" + postId, request.getContent());
    //destination + message.getPostId() 은 채팅장소를 구분하는 값
    //Client에서는 해당 주소를 SUBSCRIBE하고 있다가 이 주소에 메세지가 전달되면 화면에 출력당함.
    //convertAndSend(): 해당 주소로 받은 메세지를 해당 채팅방의 모든 구독자에게 브로드캐스트 (실시간 서버 -> 클라이언트 전달함으로써 실시간 대화 가능)


    chatMessageRepository.save(
        ChatMessage.builder()
            .postId(post.getPostId())
            .memberId(chatMemberInfo.getMemberId())
            .nickname(chatMemberInfo.getNickname())
            .contents(request.getContent())
            .createdAt(LocalDateTime.now())
            .isSent(true)
            .build()
    );

  }


}





