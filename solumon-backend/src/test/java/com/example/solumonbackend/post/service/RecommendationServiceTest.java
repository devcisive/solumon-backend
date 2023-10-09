package com.example.solumonbackend.post.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.repository.RecommendRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
  @Mock
  private RecommendRepository recommendRepository;

  @InjectMocks
  private RecommendationService recommendationService;

  Member testMember;
  Post post1, post2, post3, post5, post6, post7;
  double score1, score2, score3, score5, score6, score7;
  Recommend r1, r2, r3, r5, r6, r7;
/*
 유사도: post1 = post2 -> post3 && post5 = post6 -> post7
 ONGOING: post1의 태그: 태그1, 태그2 / post2의 태그: 태그2, 태그3 / post3의 태그: 태그3 / post4의 태그: 없음
    - 최신순 예상 순서: post2 -> post1 -> post3
    - 투표 마감 임박 예상 순서: post1 -> post2 -> post3
    - 투표 참여자순 예상 순서: post1 -> post2 -> post3
    - 채팅 참여자순 예상 순서: post2 -> post1 -> post3
 COMPLETED: post5의 태그: 태그1, 태그2 / post6의 태그: 태그2, 태그3 / post7의 태그: 태그3 / post8의 태그: 없음
    - 최신순 예상 순서: post6 -> post5 -> post7
    - 투표 마감 임박 예상 순서: post5 -> post6 -> post7
    - 투표 참여자순 예상 순서: post5 -> post6 -> post7
    - 채팅 참여자순 예상 순서: post6 -> post5 -> post7
 */

  @BeforeEach
  public void setUp() {
    testMember = Member.builder()
        .email("sample@sample.com")
        .build();

    post1 = Post.builder()
        .postId(1L)
        .title("post1")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(2))
        .endAt(LocalDateTime.now().plusDays(1))
        .voteCount(10)
        .chatCount(5)
        .build();

    post2 = Post.builder()
        .postId(2L)
        .title("post2")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(1))
        .endAt(LocalDateTime.now().plusDays(2))
        .voteCount(0)
        .chatCount(10)
        .build();

    post3 = Post.builder()
        .postId(3L)
        .title("post3")
        .member(testMember)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().plusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    post5 = Post.builder()
        .postId(5L)
        .title("post5")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(2))
        .endAt(LocalDateTime.now().minusDays(1))
        .voteCount(10)
        .chatCount(5)
        .build();

    post6 = Post.builder()
        .postId(6L)
        .title("post6")
        .member(testMember)
        .createdAt(LocalDateTime.now().minusDays(1))
        .endAt(LocalDateTime.now().minusDays(2))
        .voteCount(0)
        .chatCount(10)
        .build();

    post7 = Post.builder()
        .postId(7L)
        .title("post7")
        .member(testMember)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().minusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

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
  }

  @Test
  void recommend_success_ongoing_latest() {
    //given
   given(recommendRepository.findAllByMemberId(testMember.getMemberId()))
       .willReturn(List.of(r1, r2, r3, r5, r6, r7));

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.ONGOING, PostOrder.LATEST, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals("post2", response.getContent().get(0).getTitle());
    Assertions.assertEquals("post1", response.getContent().get(1).getTitle());
    Assertions.assertEquals("post3", response.getContent().get(2).getTitle());

    verify(recommendRepository, times(1)).findAllByMemberId(testMember.getMemberId());
  }

  @Test
  void recommend_success_ongoing_imminentDeadline() {
    //given
    given(recommendRepository.findAllByMemberId(testMember.getMemberId()))
        .willReturn(List.of(r1, r2, r3, r5, r6, r7));

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(
            testMember, PostStatus.ONGOING, PostOrder.IMMINENT_DEADLINE, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals("post1", response.getContent().get(0).getTitle());
    Assertions.assertEquals("post2", response.getContent().get(1).getTitle());
    Assertions.assertEquals("post3", response.getContent().get(2).getTitle());

    verify(recommendRepository, times(1)).findAllByMemberId(testMember.getMemberId());
  }

  @Test
  void recommend_success_ongoing_mostVotes() {
    //given
    given(recommendRepository.findAllByMemberId(testMember.getMemberId()))
        .willReturn(List.of(r1, r2, r3, r5, r6, r7));

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.ONGOING, PostOrder.MOST_VOTES, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals("post1", response.getContent().get(0).getTitle());
    Assertions.assertEquals("post2", response.getContent().get(1).getTitle());
    Assertions.assertEquals("post3", response.getContent().get(2).getTitle());

    verify(recommendRepository, times(1)).findAllByMemberId(testMember.getMemberId());}

  @Test
  void recommend_success_ongoing_mostChatParticipants() {
    //given
    given(recommendRepository.findAllByMemberId(testMember.getMemberId()))
        .willReturn(List.of(r1, r2, r3, r5, r6, r7));

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.ONGOING, PostOrder.MOST_CHAT_PARTICIPANTS, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals("post2", response.getContent().get(0).getTitle());
    Assertions.assertEquals("post1", response.getContent().get(1).getTitle());
    Assertions.assertEquals("post3", response.getContent().get(2).getTitle());

    verify(recommendRepository, times(1)).findAllByMemberId(testMember.getMemberId());
  }

  @Test
  void recommend_success_completed_latest() {
    //given
    given(recommendRepository.findAllByMemberId(testMember.getMemberId()))
        .willReturn(List.of(r1, r2, r3, r5, r6, r7));

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.COMPLETED, PostOrder.LATEST, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals("post6", response.getContent().get(0).getTitle());
    Assertions.assertEquals("post5", response.getContent().get(1).getTitle());
    Assertions.assertEquals("post7", response.getContent().get(2).getTitle());

    verify(recommendRepository, times(1)).findAllByMemberId(testMember.getMemberId());
  }

  @Test
  void recommend_success_completed_mostVotes() {
    //given
    given(recommendRepository.findAllByMemberId(testMember.getMemberId()))
        .willReturn(List.of(r1, r2, r3, r5, r6, r7));


    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.COMPLETED, PostOrder.MOST_VOTES, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals("post5", response.getContent().get(0).getTitle());
    Assertions.assertEquals("post6", response.getContent().get(1).getTitle());
    Assertions.assertEquals("post7", response.getContent().get(2).getTitle());

    verify(recommendRepository, times(1)).findAllByMemberId(testMember.getMemberId());
  }

  @Test
  void recommend_success_completed_mostChatParticipants() {
    //given
    given(recommendRepository.findAllByMemberId(testMember.getMemberId()))
        .willReturn(List.of(r1, r2, r3, r5, r6, r7));


    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.COMPLETED, PostOrder.MOST_CHAT_PARTICIPANTS, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals("post6", response.getContent().get(0).getTitle());
    Assertions.assertEquals("post5", response.getContent().get(1).getTitle());
    Assertions.assertEquals("post7", response.getContent().get(2).getTitle());

    verify(recommendRepository, times(1)).findAllByMemberId(testMember.getMemberId());
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