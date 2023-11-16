package com.example.solumonbackend.post.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.RecommendRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import com.example.solumonbackend.post.service.RecommendationService;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import com.example.solumonbackend.post.type.PostType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails(value = "sample@sample.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class PostControllerTest_Recommend {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private RecommendationService recommendationService;
  @Autowired
  private MemberTagRepository memberTagRepository;
  @Autowired
  private PostTagRepository postTagRepository;
  @Autowired
  private RecommendRepository recommendRepository;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private TagRepository tagRepository;

  Member testMember;
  Post post1, post2, post3, post4, post5, post6, post7, post8;
  double score1, score2, score3, score5, score6, score7;
  Recommend r1, r2, r3, r5, r6, r7;

  Post deletedPost;
  Recommend rd;


/*
 testMember의 관심 태그: 태그1, 태그2, 태그3
 유사도: post1 = post2 -> post3 && post5 = post6 -> post7
 ONGOING: post1의 태그: 태그1, 태그2 / post2의 태그: 태그2, 태그3 / post3의 태그: 태그3 / post4의 태그: 없음
    - 최신순 예상 순서: post2 -> post1 -> post3
    - 투표 마감 임박 예상 순서: post1 -> post2 -> post3
    - 투표 참여자순 예상 순서: post1 -> post2 -> post3
    - 채팅 참여자순 예상 순서: post2 -> post1 -> post3
 COMPLETED: post5의 태그: 태그1, 태그2 / post6의 태그: 태그2, 태그3 / post7의 태그: 태그3 / post8의 태그: 없음
    - 최신순 예상 순서: post6 -> post5 -> post7
    - 투표 참여자순 예상 순서: post5 -> post6 -> post7
    - 채팅 참여자순 예상 순서: post6 -> post5 -> post7
 */

  @BeforeEach
  public void setUp() {
    testMember = Member.builder()
        .email("sample@sample.com")
        .build();

    memberRepository.save(testMember);

    post1 = Post.builder()
        .title("post1")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(2))
        .endAt(LocalDateTime.now().plusDays(1))
        .voteCount(10)
        .chatCount(5)
        .build();

    post2 = Post.builder()
        .title("post2")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(1))
        .endAt(LocalDateTime.now().plusDays(2))
        .voteCount(0)
        .chatCount(10)
        .build();

    post3 = Post.builder()
        .title("post3")
        .member(testMember)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().plusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    post4 = Post.builder()
        .title("post4")
        .member(testMember)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().plusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    post5 = Post.builder()
        .title("post5")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(2))
        .endAt(LocalDateTime.now().minusDays(1))
        .voteCount(10)
        .chatCount(5)
        .build();

    post6 = Post.builder()
        .title("post6")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(1))
        .endAt(LocalDateTime.now().minusDays(2))
        .voteCount(0)
        .chatCount(10)
        .build();

    post7 = Post.builder()
        .title("post7")
        .member(testMember)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().minusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    post8 = Post.builder()
        .title("post8")
        .member(testMember)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().minusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    postRepository.saveAll(List.of(post1, post2, post3, post4, post5, post6, post7, post8));

    double[] targetVector = new double[] {1, 1, 1};

    score1 = calculateCosineSimilarity(targetVector, new double[] {1, 1, 0});
    score2 = calculateCosineSimilarity(targetVector, new double[] {0, 1, 1});
    score3 = calculateCosineSimilarity(targetVector, new double[] {0, 0, 1});
    score5 = calculateCosineSimilarity(targetVector, new double[] {1, 1, 0});
    score6 = calculateCosineSimilarity(targetVector, new double[] {0, 1, 1});
    score7 = calculateCosineSimilarity(targetVector, new double[] {0, 0, 1});

    r1 = Recommend.builder()
        .memberId(testMember.getMemberId())
        .post(post1)
        .score(score1)
        .build();
    r2 = Recommend.builder()
        .memberId(testMember.getMemberId())
        .post(post2)
        .score(score2)
        .build();
    r3 = Recommend.builder()
        .memberId(testMember.getMemberId())
        .post(post3)
        .score(score3)
        .build();
    r5 = Recommend.builder()
        .memberId(testMember.getMemberId())
        .post(post5)
        .score(score5)
        .build();
    r6 = Recommend.builder()
        .memberId(testMember.getMemberId())
        .post(post6)
        .score(score6)
        .build();
    r7 = Recommend.builder()
        .memberId(testMember.getMemberId())
        .post(post7)
        .score(score7)
        .build();

    recommendRepository.saveAll(List.of(r1, r2, r3, r5, r6, r7));


    deletedPost = Post.builder()
        .title("deletedPost")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(2))
        .endAt(LocalDateTime.now().plusDays(1))
        .voteCount(10)
        .chatCount(5)
        .postStatus(PostStatus.DELETED)
        .build();

    rd = Recommend.builder()
        .memberId(testMember.getMemberId())
        .post(deletedPost)
        .score(score1)
        .build();

  }

  @Test
  void recommend_success_ongoing_latest() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders.get("/posts")
            .queryParam("postType", String.valueOf(PostType.INTEREST))
            .queryParam("postStatus", String.valueOf(PostStatus.ONGOING))
            .queryParam("postOrder", String.valueOf(PostOrder.LATEST))
            .queryParam("page", "1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("post2"))
        .andExpect(jsonPath("$.content[1].title").value("post1"))
        .andExpect(jsonPath("$.content[2].title").value("post3"));
  }

  @Test
  void recommend_success_ongoing_imminentClose() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders.get("/posts")
            .queryParam("postType", String.valueOf(PostType.INTEREST))
            .queryParam("postStatus", String.valueOf(PostStatus.ONGOING))
            .queryParam("postOrder", String.valueOf(PostOrder.IMMINENT_CLOSE))
            .queryParam("page", "1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("post1"))
        .andExpect(jsonPath("$.content[1].title").value("post2"))
        .andExpect(jsonPath("$.content[2].title").value("post3"));
  }

  @Test
  void recommend_success_ongoing_mostVotes() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders.get("/posts")
            .queryParam("postType", String.valueOf(PostType.INTEREST))
            .queryParam("postStatus", String.valueOf(PostStatus.ONGOING))
            .queryParam("postOrder", String.valueOf(PostOrder.MOST_VOTES))
            .queryParam("page", "1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("post1"))
        .andExpect(jsonPath("$.content[1].title").value("post2"))
        .andExpect(jsonPath("$.content[2].title").value("post3"));
  }

  @Test
  void recommend_success_ongoing_mostChatParticipants() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders.get("/posts")
            .queryParam("postType", String.valueOf(PostType.INTEREST))
            .queryParam("postStatus", String.valueOf(PostStatus.ONGOING))
            .queryParam("postOrder", String.valueOf(PostOrder.MOST_CHAT_PARTICIPANTS))
            .queryParam("page", "1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("post2"))
        .andExpect(jsonPath("$.content[1].title").value("post1"))
        .andExpect(jsonPath("$.content[2].title").value("post3"));
  }

  @Test
  void recommend_success_completed_latest() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders.get("/posts")
            .queryParam("postType", String.valueOf(PostType.INTEREST))
            .queryParam("postStatus", String.valueOf(PostStatus.COMPLETED))
            .queryParam("postOrder", String.valueOf(PostOrder.LATEST))
            .queryParam("page", "1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("post6"))
        .andExpect(jsonPath("$.content[1].title").value("post5"))
        .andExpect(jsonPath("$.content[2].title").value("post7"));
  }

  @Test
  void recommend_success_completed_mostVotes() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders.get("/posts")
            .queryParam("postType", String.valueOf(PostType.INTEREST))
            .queryParam("postStatus", String.valueOf(PostStatus.COMPLETED))
            .queryParam("postOrder", String.valueOf(PostOrder.MOST_VOTES))
            .queryParam("page", "1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("post5"))
        .andExpect(jsonPath("$.content[1].title").value("post6"))
        .andExpect(jsonPath("$.content[2].title").value("post7"));
  }

  @Test
  void recommend_success_completed_mostChatParticipants() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders.get("/posts")
            .queryParam("postType", String.valueOf(PostType.INTEREST))
            .queryParam("postStatus", String.valueOf(PostStatus.COMPLETED))
            .queryParam("postOrder", String.valueOf(PostOrder.MOST_CHAT_PARTICIPANTS))
            .queryParam("page", "1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("post6"))
        .andExpect(jsonPath("$.content[1].title").value("post5"))
        .andExpect(jsonPath("$.content[2].title").value("post7"));
  }

  private double calculateCosineSimilarity(double[] targetVector, double[] tagVector) {
    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;
    for (int i = 0; i < tagVector.length; i++) {
      dotProduct += targetVector[i] * tagVector[i];
      normA += Math.pow(targetVector[i], 2);
      normB += Math.pow(tagVector[i], 2);
    }
    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
  }
}