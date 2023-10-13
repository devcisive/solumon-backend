package com.example.solumonbackend.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.times;

import com.example.solumonbackend.chat.entity.ChannelMember;
import com.example.solumonbackend.chat.model.ChatMemberInfo;
import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.chat.model.ChatMessageDto.Request;
import com.example.solumonbackend.chat.repository.ChannelMemberRepository;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock
  private ChannelMemberRepository channelMemberRepository;
  @Mock
  private PostRepository postRepository;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private KafkaChatService kafkaChatService;


  @InjectMocks
  private ChatService chatService;

  private Post endPost;
  private Post ongoingPost;
  private Member member;
  private ChatMemberInfo chatMemberInfo;
  private ChatMessageDto.Request request;

  @BeforeEach
  void setup() {
    endPost = Post.builder()
        .postId(1L)
        .endAt(LocalDateTime.now().minusDays(1))
        .build();

    ongoingPost = Post.builder()
        .postId(2L)
        .endAt(LocalDateTime.now().plusDays(1))
        .chatCount(0)
        .build();


    member = Member.builder()
        .memberId(1L)
        .nickname("닉네임")
        .build();

    chatMemberInfo = new ChatMemberInfo(member.getMemberId(), member.getNickname());
    request = new Request("메세지 보냅니다.");

  }

  @DisplayName("실패 - 존재하지 않는 게시글")
  @Test
  void sendChatMessage_fail_NOT_FOUND_POST() {
    // Given
    Mockito.when(postRepository.findById(100L)).thenThrow(PostException.class);


    // 예외타입과 메세지 한번에 검사
    assertThrowsExactly(PostException.class, () -> {
      chatService.sendChatMessage(100L, request, chatMemberInfo);
    });

    // Then
    Mockito.verify(postRepository, times(1)).findById(100L);

  }


  @DisplayName("실패 - 마감된 게시글")
  @Test
  void sendChatMessage_fail_POST_IS_CLOSED() {
    // Given
    Long postId = endPost.getPostId();
    Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(endPost));

    // When
    assertThrowsExactly(PostException.class, () -> {
      chatService.sendChatMessage(postId, request, chatMemberInfo);
    });

    // Then
    Mockito.verify(postRepository, times(1)).findById(postId);

  }



  @DisplayName("성공 - 해당 게시글에 채팅한 이력이 있는 경우")
  @Test
  void sendChatMessage_success_existLastChat() {
    // Given
    Long postId = ongoingPost.getPostId();
    Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(ongoingPost));
    Mockito.when(channelMemberRepository.existsByPostPostIdAndMemberMemberId(postId,member.getMemberId())).thenReturn(true);
    ArgumentCaptor<ChatMessageDto.Response> chatResponseArgumentCaptor
        = ArgumentCaptor.forClass(ChatMessageDto.Response.class);

    // When
    chatService.sendChatMessage(postId, request, chatMemberInfo);

    // Then
    Mockito.verify(postRepository, times(1)).findById(postId);
    Mockito.verify(kafkaChatService, times(1))
        .publishChatMessage(chatResponseArgumentCaptor.capture());

    ChatMessageDto.Response captorResponse = chatResponseArgumentCaptor.getValue();
    assertEquals(postId,captorResponse.getPostId());
    assertEquals(chatMemberInfo.getMemberId(),captorResponse.getMemberId());
    assertEquals(chatMemberInfo.getNickname(),captorResponse.getNickname());
    assertEquals(request.getContent(),captorResponse.getContents());
  }



  @DisplayName("성공 - 해당 게시글에 채팅한 이력이 없는 경우")
  @Test
  void sendChatMessage_success_notFoundLastChat() {
    // Given
    Long postId = ongoingPost.getPostId();
    Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(ongoingPost));
    Mockito.when(channelMemberRepository.existsByPostPostIdAndMemberMemberId(postId,member.getMemberId())).thenReturn(false);
    Mockito.when(memberRepository.findById(member.getMemberId())).thenReturn(Optional.of(member));

    ArgumentCaptor<ChannelMember> channelMemberArgumentCaptor
        = ArgumentCaptor.forClass(ChannelMember.class);

    ArgumentCaptor<ChatMessageDto.Response> chatResponseArgumentCaptor
        = ArgumentCaptor.forClass(ChatMessageDto.Response.class);

    ArgumentCaptor<Post> postArgumentCaptor
        = ArgumentCaptor.forClass(Post.class);

    // When
    chatService.sendChatMessage(postId, request, chatMemberInfo);

    // Then
    Mockito.verify(postRepository, times(1)).findById(postId);

    Mockito.verify(channelMemberRepository, times(1))
        .existsByPostPostIdAndMemberMemberId(postId ,chatMemberInfo.getMemberId());

    Mockito.verify(memberRepository, times(1)).findById(chatMemberInfo.getMemberId());
    Mockito.verify(channelMemberRepository,times(1)).save(channelMemberArgumentCaptor.capture());
    Mockito.verify(postRepository,times(1)).save(postArgumentCaptor.capture());

    Mockito.verify(kafkaChatService, times(1))
        .publishChatMessage(chatResponseArgumentCaptor.capture());

    // captureValues
    ChannelMember captureChannelMember = channelMemberArgumentCaptor.getValue();
    Post captorPost = postArgumentCaptor.getValue();
    ChatMessageDto.Response captorResponse = chatResponseArgumentCaptor.getValue();


    assertEquals(ongoingPost.getPostId(), captorPost.getPostId());
    assertEquals(ongoingPost.getChatCount(), captorPost.getChatCount());
    assertEquals(postId, captureChannelMember.getPost().getPostId());

    assertEquals(member.getMemberId(), captureChannelMember.getMember().getMemberId());

    assertEquals(postId,captorResponse.getPostId());
    assertEquals(chatMemberInfo.getMemberId(),captorResponse.getMemberId());
    assertEquals(chatMemberInfo.getNickname(),captorResponse.getNickname());
    assertEquals(request.getContent(),captorResponse.getContents());

  }



  @DisplayName("실패 - 해당 게시글에 채팅한 이력이 없는 경우 + 존재하지 않는 멤버")
  @Test
  void sendChatMessage_fail_notFoundLastChat_NOT_FOUND_MEMBER() {
    // Given
    Long postId = ongoingPost.getPostId();
    Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(ongoingPost));
    Mockito.when(channelMemberRepository.existsByPostPostIdAndMemberMemberId(postId,member.getMemberId())).thenReturn(false);
    Mockito.when(memberRepository.findById(member.getMemberId())).thenReturn(Optional.empty());


    // When
    assertThrowsExactly(MemberException.class, () -> {
      chatService.sendChatMessage(postId, request, chatMemberInfo);
    });


    // Then
    Mockito.verify(postRepository, times(1)).findById(postId);

    Mockito.verify(channelMemberRepository, times(1))
        .existsByPostPostIdAndMemberMemberId(postId ,chatMemberInfo.getMemberId());

    Mockito.verify(memberRepository, times(1)).findById(chatMemberInfo.getMemberId());
  }



}