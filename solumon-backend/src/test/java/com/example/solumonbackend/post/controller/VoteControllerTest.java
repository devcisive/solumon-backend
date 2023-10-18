package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.*;
import com.example.solumonbackend.post.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class VoteControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private VoteRepository voteRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private ChoiceRepository choiceRepository;
  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private PostTagRepository postTagRepository;

  Member member;
  Member postMember;
  Post savePost;

  @BeforeEach
  public void setUp() {
    member = Member.builder()
        .email("test@gmail.com")
        .nickname("별명")
        .role(MemberRole.GENERAL)
        .build();

    postMember = Member.builder()
        .email("test2@gmail.com")
        .nickname("별명2")
        .role(MemberRole.GENERAL)
        .build();

    memberRepository.saveAll(List.of(member, postMember));

    savePost = postRepository.save(Post.builder()
        .member(postMember)
        .title("제목")
        .contents("내용")
        .endAt(LocalDateTime.of(2023, 9, 30, 13, 22, 32).plusDays(10))
        .build());
    System.out.println(savePost.getPostId());

    Tag tag = tagRepository.save(Tag.builder().name("태그1").build());
    postTagRepository.save(PostTag.builder().post(savePost).tag(tag).build());
    choiceRepository.saveAll(List.of(
        Choice.builder()
            .post(savePost)
            .choiceNum(1)
            .choiceText("선택지1")
            .build(),
        Choice.builder()
            .post(savePost)
            .choiceNum(2)
            .choiceText("선택지2")
            .build()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표하기 성공")
  void createVote_success() throws Exception {
    //given
    int selectedNum = 1;
    String jsonRequest = objectMapper.writeValueAsString(selectedNum);

    //when
    //then
    mockMvc.perform(post("/posts/" + savePost.getPostId() + "/vote")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.choices[0].choice_text").value("선택지1"))
        .andExpect(jsonPath("$.choices[0].choice_count").value(1L))
        .andExpect(jsonPath("$.choices[0].choice_percent").value(100))
        .andExpect(jsonPath("$.choices[1].choice_text").value("선택지2"))
        .andExpect(jsonPath("$.choices[1].choice_percent").value(0));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표하기 실패 - 존재하지 않는 게시글")
  void createVote_fail_notFoundPost() throws Exception {
    //given
    int selectedNum = 1;
    String jsonRequest = objectMapper.writeValueAsString(selectedNum);

    //when
    //then
    mockMvc.perform(post("/posts/" + (savePost.getPostId() + 1) + "/vote")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_POST.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표하기 실패 - 이미 마감된 게시글")
  void createVote_fail_postIsClosed() throws Exception {
    //given
    Post closedPost = postRepository.save(Post.builder()
        .member(postMember)
        .endAt(LocalDateTime.now().minusDays(2))
        .build());

    int selectedNum = 1;
    String jsonRequest = objectMapper.writeValueAsString(selectedNum);

    //when
    //then
    mockMvc.perform(post("/posts/" + closedPost.getPostId() + "/vote")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.POST_IS_CLOSED.toString()));
  }

  @Test
  @WithUserDetails(value = "test2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표하기 실패 - 작성자가 투표")
  void createVote_fail_writerCanNotVote() throws Exception {
    //given
    int selectedNum = 1;
    String jsonRequest = objectMapper.writeValueAsString(selectedNum);

    //when
    //then
    mockMvc.perform(post("/posts/" + savePost.getPostId() + "/vote")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.WRITER_CAN_NOT_VOTE.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표하기 실패 - 이미 투표를 함")
  void createVote_fail_voteOnlyOnce() throws Exception {
    //given
    voteRepository.save(Vote.builder()
        .member(member)
        .post(savePost)
        .selectedNum(1)
        .build());

    int selectedNum = 1;
    String jsonRequest = objectMapper.writeValueAsString(selectedNum);

    //when
    //then
    mockMvc.perform(post("/posts/" + savePost.getPostId() + "/vote")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.VOTE_ONLY_ONCE.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표취소 성공")
  void deleteVote_success() throws Exception {
    //given
    voteRepository.save(Vote.builder()
        .selectedNum(1)
        .post(savePost)
        .member(member)
        .build());

    //when
    //then
    mockMvc.perform(delete("/posts/" + savePost.getPostId() + "/vote"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("투표가 취소되었습니다."));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표취소 실패 - 존재하지 않는 게시글")
  void deleteVote_fail_notFoundPost() throws Exception {
    //given
    voteRepository.save(Vote.builder()
        .selectedNum(1)
        .post(savePost)
        .member(member)
        .build());

    //when
    //then
    mockMvc.perform(delete("/posts/" + (savePost.getPostId() + 1) + "/vote"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_POST.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표취소 실패 - 이미 마감된 게시글")
  void deleteVote_fail_postIsClosed() throws Exception {
    //given
    voteRepository.save(Vote.builder()
        .selectedNum(1)
        .post(savePost)
        .member(member)
        .build());

    Post closedPost = postRepository.save(Post.builder()
        .member(postMember)
        .endAt(LocalDateTime.now().minusDays(2))
        .build());

    //when
    //then
    mockMvc.perform(delete("/posts/" + closedPost.getPostId() + "/vote"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.POST_IS_CLOSED.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("투표취소 실패 - 투표 안함")
  void deleteVote_fail_onlyPersonWhoVoted() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(delete("/posts/" + savePost.getPostId() + "/vote"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ONLY_THE_PERSON_WHO_VOTED_CAN_CANCEL.toString()));
  }

}