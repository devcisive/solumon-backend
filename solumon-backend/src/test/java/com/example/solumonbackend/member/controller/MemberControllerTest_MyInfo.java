package com.example.solumonbackend.member.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solumonbackend.chat.entity.ChannelMember;
import com.example.solumonbackend.chat.repository.ChannelMemberRepository;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.entity.Vote;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import com.example.solumonbackend.post.repository.VoteRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithUserDetails(value = "fakeMember@naver.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class MemberControllerTest_MyInfo {


  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private MemberTagRepository memberTagRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private VoteRepository voteRepository;
  @Autowired
  private ChannelMemberRepository channelMemberRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  private Member fakeMember;
  private Member otherMember;
  private Tag fakeTag;
  private MemberTag fakeMemberTag;

  List<Post> myWritePosts;
  Pair<List<Post>, List<Vote>> postsAndMyVotes;
  Pair<List<Post>, List<ChannelMember>> postsAndMyChats;

  @BeforeEach
  public void dataSetup() {

    // 기준이 될 멤버
    fakeMember = Member.builder()
        .email("fakeMember@naver.com")
        .nickname("닉네임")
        .registeredAt(LocalDateTime.now())
        .role(MemberRole.GENERAL)
        .modifiedAt(null)
        .unregisteredAt(null)
        .password(passwordEncoder.encode("password"))
        .isFirstLogIn(true)
        .build();

    otherMember = Member.builder()
        .email("fakeMember2@naver.com")
        .nickname("새로운닉네임")
        .registeredAt(LocalDateTime.now())
        .role(MemberRole.GENERAL)
        .modifiedAt(null)
        .unregisteredAt(null)
        .password(passwordEncoder.encode("password"))
        .isFirstLogIn(true)
        .build();

    // 태그, 멤버태그
    fakeTag = Tag.builder().name("태그1").build();
    fakeMemberTag = MemberTag.builder().member(fakeMember).tag(fakeTag).build();

    // 내가 작성한 글
    myWritePosts = new ArrayList<>();
    myWritePosts.add(
        Post.builder()
            .member(fakeMember)
            .title("myWritePost1")
            .contents("최신5,마감,투표5,채팅1")
            .createdAt(LocalDateTime.now().minusDays(5))
            .endAt(LocalDateTime.now().minusDays(1))
            .voteCount(10)
            .chatCount(50)
            .build());

    myWritePosts.add(
        Post.builder()
            .member(fakeMember)
            .title("myWritePost2")
            .contents("최신4,마감,투표4,채팅2")
            .createdAt(LocalDateTime.now().minusDays(4))
            .endAt(LocalDateTime.now().minusDays(1))
            .voteCount(20)
            .chatCount(40)
            .build());

    myWritePosts.add(
        Post.builder()
            .member(fakeMember)
            .title("myWritePost3")
            .contents("최신3,마감,투표3,채팅3")
            .createdAt(LocalDateTime.now().minusDays(3))
            .endAt(LocalDateTime.now().minusDays(1))
            .voteCount(30)
            .chatCount(30)
            .build());

    myWritePosts.add(
        Post.builder().member(fakeMember)
            .title("myWritePost4")
            .contents("최신2,진행,투표2,채팅4")
            .createdAt(LocalDateTime.now().minusDays(1))
            .endAt(LocalDateTime.now().plusDays(1))
            .voteCount(40)
            .chatCount(20)
            .build());

    myWritePosts.add(
        Post.builder()
            .member(fakeMember)
            .title("myWritePost5")
            .contents("최신1,진행,투표1,채팅5")
            .createdAt(LocalDateTime.now().minusDays(1))
            .endAt(LocalDateTime.now().plusDays(1))
            .voteCount(50)
            .chatCount(10)
            .build());

    // 내가 투표참여한 글, 내 투표
    List<Post> myVotePosts = new ArrayList<>();
    List<Vote> myVotes;

    myVotePosts.add(
        Post.builder()
            .member(otherMember)
            .title("myVotePost1")
            .contents("최신5,마감,투표5,채팅1")
            .createdAt(LocalDateTime.now().minusDays(5))
            .endAt(LocalDateTime.now().plusDays(1))
            .voteCount(10)
            .chatCount(50)
            .build());

    myVotePosts.add(
        Post.builder()
            .member(otherMember)
            .title("myVotePost2")
            .contents("최신4,마감,투표4,채팅2")
            .createdAt(LocalDateTime.now().minusDays(4))
            .endAt(LocalDateTime.now().plusDays(1))
            .voteCount(20)
            .chatCount(40)
            .build());

    myVotePosts.add(
        Post.builder()
            .member(otherMember)
            .title("myVotePost3")
            .contents("최신3,마감,투표3,채팅3")
            .createdAt(LocalDateTime.now().minusDays(3))
            .endAt(LocalDateTime.now().plusDays(1))
            .voteCount(30)
            .chatCount(30)
            .build());

    myVotePosts.add(
        Post.builder()
            .member(otherMember)
            .title("myVotePost4")
            .contents("최신2,진행,투표2,채팅4")
            .createdAt(LocalDateTime.now().minusDays(1))
            .endAt(LocalDateTime.now().minusDays(1))
            .voteCount(40)
            .chatCount(20)
            .build());

    myVotePosts.add(
        Post.builder().member(otherMember)
            .title("myVotePost5")
            .contents("최신1,진행,투표1,채팅5")
            .createdAt(LocalDateTime.now().minusDays(1))
            .endAt(LocalDateTime.now().minusDays(1))
            .voteCount(50)
            .chatCount(10)
            .build());

    myVotes = myVotePosts.stream()
        .map(post -> Vote.builder().post(post).member(fakeMember).build())
        .collect(Collectors.toList());

    postsAndMyVotes = Pair.of(myVotePosts, myVotes);

    // 내가 채팅참여한 글, 내 채팅
    List<Post> myChatPosts = new ArrayList<>();
    List<ChannelMember> myChats;

    myChatPosts.add(
        Post.builder()
            .member(otherMember)
            .title("myChatPost1")
            .contents("최신5,마감,투표5,채팅1")
            .createdAt(LocalDateTime.now().minusDays(5))
            .endAt(LocalDateTime.now().plusDays(1))
            .voteCount(10)
            .chatCount(50)
            .build());

    myChatPosts.add(
        Post.builder()
            .member(otherMember).
            title("myChatPost2").contents("최신4,마감,투표4,채팅2")
            .createdAt(LocalDateTime.now().minusDays(4))
            .endAt(LocalDateTime.now().plusDays(1))
            .voteCount(20)
            .chatCount(40)
            .build());

    myChatPosts.add(
        Post.builder().member(otherMember)
            .title("myChatPost3")
            .contents("최신3,마감,투표3,채팅3")
            .createdAt(LocalDateTime.now().minusDays(3))
            .endAt(LocalDateTime.now().plusDays(1))
            .voteCount(30)
            .chatCount(30)
            .build());

    myChatPosts.add(
        Post.builder()
            .member(otherMember)
            .title("myChatPost4")
            .contents("최신2,진행,투표2,채팅4")
            .createdAt(LocalDateTime.now().minusDays(1))
            .endAt(LocalDateTime.now().minusDays(1))
            .voteCount(40)
            .chatCount(20)
            .build());

    myChatPosts.add(
        Post.builder()
            .member(otherMember)
            .title("myChatPost5")
            .contents("최신1,진행,투표1,채팅5")
            .createdAt(LocalDateTime.now().minusDays(1))
            .endAt(LocalDateTime.now().minusDays(1))
            .voteCount(50)
            .chatCount(10)
            .build());

    myChats = myChatPosts.stream()
        .map(post -> ChannelMember.builder().post(post).member(fakeMember).build())
        .collect(Collectors.toList());

    postsAndMyChats = Pair.of(myChatPosts, myChats);



    // 저장 순서 중요
    memberRepository.save(fakeMember);
    memberRepository.save(otherMember);
    tagRepository.save(fakeTag);
    memberTagRepository.save(fakeMemberTag);

    postRepository.saveAll(myWritePosts);
    postRepository.saveAll(postsAndMyVotes.getFirst());
    postRepository.saveAll(postsAndMyChats.getFirst());

    voteRepository.saveAll(postsAndMyVotes.getSecond());
    channelMemberRepository.saveAll(postsAndMyChats.getSecond());

  }


  @DisplayName("내 정보 가져오기 - 성공")
  @Test
  void getMyInfo_Success() throws Exception {

    mockMvc.perform(get("/user")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.memberId").value(fakeMember.getMemberId()))
        .andExpect(jsonPath("$.nickname").value(fakeMember.getNickname()))
        .andExpect(jsonPath("$.email").value(fakeMember.getEmail()))
        .andExpect(jsonPath("$.interests[0]").value(fakeMemberTag.getTag().getName()));

  }


  @DisplayName("내 정보 수정 - 성공(전체수정)")
  @Test
  void updateMyInfo_success_all() throws Exception {
    MemberUpdateDto.Request request = MemberUpdateDto.Request.builder()
        .nickname("pass새로운닉네임")
        .password("password")
        .newPassword1("1newPassword!")
        .newPassword2("1newPassword!")
        .build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.put("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk()) //400
        .andExpect(jsonPath("$.memberId").value(fakeMember.getMemberId()))
        .andExpect(jsonPath("$.nickname").value(request.getNickname()))
        .andExpect(jsonPath("$.interests[0]").value(fakeMemberTag.getTag().getName()));
  }


  @DisplayName("내 정보 수정 - 실패 (기존 비밀번호 불일치)")
  @Test
  void updateMyInfo_fail_NOT_CORRECT_PASSWORD() throws Exception {
    MemberUpdateDto.Request request = MemberUpdateDto.Request.builder()
        .nickname("새로운닉네임")
        .password("wrongPassword")
        .newPassword1("1newPassword!")
        .newPassword2("1newPassword!")
        .build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.put("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_CORRECT_PASSWORD.toString()))
    ;
  }


  @DisplayName("내 정보 수정 - 실패 (사용중인 닉네임)")
  @Test
  void updateMyInfo_fail_ALREADY_REGISTERED_NICKNAME() throws Exception {
    MemberUpdateDto.Request request = MemberUpdateDto.Request.builder()
        .nickname("새로운닉네임")
        .password("password")
        .build();

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.put("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ALREADY_REGISTERED_NICKNAME.toString()))
    ;
  }


  @DisplayName("회원탈퇴 - 성공")
  @Test
  void withdrawMember_success() throws Exception {
    WithdrawDto.Request request = new WithdrawDto.Request("password");

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.delete("/user/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk()) //400
        .andExpect(jsonPath("$.memberId").value(fakeMember.getMemberId()))
        .andExpect(jsonPath("$.email").value(fakeMember.getEmail()))
        .andExpect(jsonPath("$.nickname").value(fakeMember.getNickname()));
  }


  @DisplayName("회원탈퇴 - 실패(비밀번호 불일치)")
  @Test
  void withdrawMember() throws Exception {
    WithdrawDto.Request request = new WithdrawDto.Request("incorrectPassword");

    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(MockMvcRequestBuilders.delete("/user/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_CORRECT_PASSWORD.toString()));
  }


  @DisplayName("관심주제 선택 - 성공")
  @Test
  void registerInterest_success() throws Exception {

    MemberInterestDto.Request request = new MemberInterestDto.Request(List.of("태그1"));
    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/interests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.memberId").value(fakeMember.getMemberId()))
        .andExpect(jsonPath("$.interests[0]").value(fakeMemberTag.getTag().getName()));
    ;
  }


  @DisplayName("관심주제 선택 - 실패(존재하지 않는 태그)")
  @Test
  void registerInterest_fail() throws Exception {

    MemberInterestDto.Request request = new MemberInterestDto.Request(List.of("태그1", "없는태그"));
    String json = objectMapper.writeValueAsString(request);

    mockMvc.perform(post("/user/interests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_TAG.toString()))
    ;
  }


  // 따로 실행하면 통과
  @DisplayName("내가 참여한 글 가져오기(성공) - <작성 - 마감 - 최신순>") // 3,2,1번
  @Test
  void getMyParticipatePosts_success_WRITE_COMPLETED_POST_ORDER() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/user/mylog")
            .param("postParticipateType", String.valueOf(PostParticipateType.WRITE))
            .param("postStatus", String.valueOf(PostStatus.COMPLETED))
            .param("postOrder", String.valueOf(PostOrder.LATEST))
            .param("page", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value(myWritePosts.get(2).getTitle()))
        .andExpect(jsonPath("$.content[0].contents").value(myWritePosts.get(2).getContents()))
        .andExpect(jsonPath("$.content[0].voteCount").value(myWritePosts.get(2).getVoteCount()))
        .andExpect(jsonPath("$.content[0].chatCount").value(myWritePosts.get(2).getChatCount()))
        .andExpect(jsonPath("$.content[1].title").value(myWritePosts.get(1).getTitle()))
        .andExpect(jsonPath("$.content[1].contents").value(myWritePosts.get(1).getContents()))
        .andExpect(jsonPath("$.content[1].voteCount").value(20))
        .andExpect(jsonPath("$.content[1].chatCount").value(40))
        .andExpect(jsonPath("$.content[2].title").value(myWritePosts.get(0).getTitle()))
        .andExpect(jsonPath("$.content[2].contents").value(myWritePosts.get(0).getContents()))
        .andExpect(jsonPath("$.content[2].voteCount").value(myWritePosts.get(0).getVoteCount()))
        .andExpect(jsonPath("$.content[2].chatCount").value(myWritePosts.get(0).getChatCount()))
    ;
  }


  @DisplayName("내가 참여한 글 가져오기(성공) - <투표 - 진행중 - 투표참여순>") // 3,2,1번
  @Test
  void getMyParticipatePosts_success_VOTE_ONGOING_MOST_VOTES() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/user/mylog")
            .param("postParticipateType", String.valueOf(PostParticipateType.VOTE))
            .param("postStatus", String.valueOf(PostStatus.ONGOING))
            .param("postOrder", String.valueOf(PostOrder.MOST_VOTES))
            .param("page", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value(postsAndMyVotes.getFirst().get(2).getTitle()))
        .andExpect(jsonPath("$.content[0].contents").value(postsAndMyVotes.getFirst().get(2).getContents()))
        .andExpect(jsonPath("$.content[0].voteCount").value(postsAndMyVotes.getFirst().get(2).getVoteCount()))
        .andExpect(jsonPath("$.content[0].chatCount").value(postsAndMyVotes.getFirst().get(2).getChatCount()))
        .andExpect(jsonPath("$.content[1].title").value(postsAndMyVotes.getFirst().get(1).getTitle()))
        .andExpect(jsonPath("$.content[1].contents").value(postsAndMyVotes.getFirst().get(1).getContents()))
        .andExpect(jsonPath("$.content[1].voteCount").value(postsAndMyVotes.getFirst().get(1).getVoteCount()))
        .andExpect(jsonPath("$.content[1].chatCount").value(postsAndMyVotes.getFirst().get(1).getChatCount()))
        .andExpect(jsonPath("$.content[2].title").value(postsAndMyVotes.getFirst().get(0).getTitle()))
        .andExpect(jsonPath("$.content[2].contents").value(postsAndMyVotes.getFirst().get(0).getContents()))
        .andExpect(jsonPath("$.content[2].voteCount").value(postsAndMyVotes.getFirst().get(0).getVoteCount()))
        .andExpect(jsonPath("$.content[2].chatCount").value(postsAndMyVotes.getFirst().get(0).getChatCount()))
    ;
  }

  @DisplayName("내가 참여한 글 가져오기(성공) - <채팅 - 마감 - 채팅참여순>") // 4,5번
  @Test
  void getMyParticipatePosts_success_VOTE_COMPLETED_MOST_MOST_CHAT_PARTICIPANTS() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/user/mylog")
            .param("postParticipateType", String.valueOf(PostParticipateType.CHAT))
            .param("postStatus", String.valueOf(PostStatus.COMPLETED))
            .param("postOrder", String.valueOf(PostOrder.MOST_CHAT_PARTICIPANTS))
            .param("page", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value(postsAndMyChats.getFirst().get(3).getTitle()))
        .andExpect(jsonPath("$.content[0].contents").value(postsAndMyChats.getFirst().get(3).getContents()))
        .andExpect(jsonPath("$.content[0].voteCount").value(postsAndMyChats.getFirst().get(3).getVoteCount()))
        .andExpect(jsonPath("$.content[0].chatCount").value(postsAndMyChats.getFirst().get(3).getChatCount()))
        .andExpect(jsonPath("$.content[1].title").value(postsAndMyChats.getFirst().get(4).getTitle()))
        .andExpect(jsonPath("$.content[1].contents").value(postsAndMyChats.getFirst().get(4).getContents()))
        .andExpect(jsonPath("$.content[1].voteCount").value(postsAndMyChats.getFirst().get(4).getVoteCount()))
        .andExpect(jsonPath("$.content[1].chatCount").value(postsAndMyChats.getFirst().get(4).getChatCount()))
    ;
  }

  @DisplayName("내가 참여한 글 가져오기(실패) - <없는 페이지>")
  @Test
  void getMyParticipatePosts_fail_nonePage() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/user/mylog")
            .param("postParticipateType", String.valueOf(PostParticipateType.CHAT))
            .param("postStatus", String.valueOf(PostStatus.COMPLETED))
            .param("postOrder", String.valueOf(PostOrder.MOST_CHAT_PARTICIPANTS))
            .param("page", "3")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0]").doesNotExist());
        // 현재 서비스 로직이 빈 페이지일 경우 빈 리스트 반환만 해놓는 상태(예외처리 안해놓은 상태)
  }


}

