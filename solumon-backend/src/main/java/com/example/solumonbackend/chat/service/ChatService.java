package com.example.solumonbackend.chat.service;

import com.example.solumonbackend.chat.entity.ChannelMember;
import com.example.solumonbackend.chat.model.ChatMemberInfo;
import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.chat.model.ChatMessageDto.Response;
import com.example.solumonbackend.chat.repository.ChannelMemberRepository;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.repository.PostRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {


  private final ChannelMemberRepository channelMemberRepository;
  private final PostRepository postRepository;
  private final MemberRepository memberRepository;
  private final KafkaChatService kafkaChatService;


  @Transactional
  public void sendChatMessage(long postId,
      ChatMessageDto.Request request, ChatMemberInfo chatMemberInfo) {

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostException(ErrorCode.NOT_FOUND_POST));

    if (post.getEndAt().isBefore(LocalDateTime.now())) {
      throw new PostException(ErrorCode.POST_IS_CLOSED);
    }

    // 해당 게시물에 채팅한 이력이 없다면 channelMember 생성, post의 chatCount 증가
    if (!channelMemberRepository.existsByPostPostIdAndMemberMemberId(postId,
        chatMemberInfo.getMemberId())) {

      Member member = memberRepository.findById(chatMemberInfo.getMemberId())
          .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

      channelMemberRepository.save(ChannelMember.builder().post(post).member(member).build());

      post.setChatCount(post.getChatCount() + 1); // 이 부분도 동시성문제?
      postRepository.save(post);
    }

    ChatMessageDto.Response sendMessage = Response.builder()
        .postId(postId)
        .memberId(chatMemberInfo.getMemberId())
        .nickname(chatMemberInfo.getNickname())
        .contents(request.getContent())
        .createdAt(LocalDateTime.now())
        .build();

    kafkaChatService.publishChatMessage(sendMessage);

  }


}