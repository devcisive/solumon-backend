package com.example.solumonbackend.post.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.RecommendRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
  @Mock
  private MemberTagRepository memberTagRepository;
  @Mock
  private PostTagRepository postTagRepository;
  @Mock
  private RecommendRepository recommendRepository;

  @InjectMocks
  private RecommendationService recommendationService;

  Member testMember;
  Tag tag1, tag2, tag3;
  MemberTag memberTag1, memberTag2, memberTag3;
  Post post1, post2, post3, post4, post5, post6, post7, post8;
  PostTag postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10;
  double score1, score2, score3, score4, score5, score6, score7, score8;
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
    - 투표 마감 임박 예상 순서: post5 -> post6 -> post7
    - 투표 참여자순 예상 순서: post5 -> post6 -> post7
    - 채팅 참여자순 예상 순서: post6 -> post5 -> post7
 */

  @BeforeEach
  public void setUp() {
    testMember = Member.builder()
        .email("sample@sample.com")
        .build();

    tag1 = Tag.builder()
        .name("태그1")
        .build();

    tag2 = Tag.builder()
        .name("태그2")
        .build();

    tag3 = Tag.builder()
        .name("태그3")
        .build();

    memberTag1 = MemberTag.builder()
        .member(testMember)
        .tag(tag1)
        .build();

    memberTag2 = MemberTag.builder()
        .member(testMember)
        .tag(tag2)
        .build();

    memberTag3 = MemberTag.builder()
        .member(testMember)
        .tag(tag3)
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

    post4 = Post.builder()
        .postId(4L)
        .title("post4")
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

    post8 = Post.builder()
        .postId(8L)
        .title("post8")
        .member(testMember)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().minusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    postTag1 = PostTag.builder()
        .post(post1)
        .tag(tag1)
        .build();

    postTag2 = PostTag.builder()
        .post(post1)
        .tag(tag2)
        .build();

    postTag3 = PostTag.builder()
        .post(post2)
        .tag(tag2)
        .build();

    postTag4 = PostTag.builder()
        .post(post2)
        .tag(tag3)
        .build();

    postTag5 = PostTag.builder()
        .post(post3)
        .tag(tag3)
        .build();

    postTag6 = PostTag.builder()
        .post(post5)
        .tag(tag1)
        .build();

    postTag7 = PostTag.builder()
        .post(post5)
        .tag(tag2)
        .build();

    postTag8 = PostTag.builder()
        .post(post6)
        .tag(tag2)
        .build();

    postTag9 = PostTag.builder()
        .post(post6)
        .tag(tag3)
        .build();

    postTag10 = PostTag.builder()
        .post(post7)
        .tag(tag3)
        .build();

    double[] targetVector = new double[] {1, 1, 1};

    score1 = calculateCosineSimilarity(targetVector, new double[] {1, 1, 0});
    score2 = calculateCosineSimilarity(targetVector, new double[] {0, 1, 1});
    score3 = calculateCosineSimilarity(targetVector, new double[] {0, 0, 1});
    score3 = calculateCosineSimilarity(targetVector, new double[] {0, 0, 0});
    score5 = calculateCosineSimilarity(targetVector, new double[] {1, 1, 0});
    score6 = calculateCosineSimilarity(targetVector, new double[] {0, 1, 1});
    score7 = calculateCosineSimilarity(targetVector, new double[] {0, 0, 1});
    score8 = calculateCosineSimilarity(targetVector, new double[] {0, 0, 0});
  }

  @Test
  void recommend_success_ongoing_latest() {
    //given
    given(memberTagRepository.findAllByMember_MemberId(testMember.getMemberId()))
        .willReturn(List.of(memberTag1, memberTag2, memberTag3));
    given(postTagRepository.findDistinctByTagIn(List.of(tag1, tag2, tag3)))
        .willReturn(List.of(postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10));

    given(postTagRepository.findAllByPost(post1)).willReturn(List.of(postTag1, postTag2));
    given(postTagRepository.findAllByPost(post2)).willReturn(List.of(postTag3, postTag4));
    given(postTagRepository.findAllByPost(post3)).willReturn(List.of(postTag5));

    given(recommendRepository.findAll(
        Sort.by(Direction.DESC, "score", "post." + PostOrder.LATEST.getSortingCriteria())))
            .willReturn(List.of(Recommend.builder()
                                    .post(post2)
                                    .score(score2)
                                    .build(),
                                Recommend.builder()
                                    .post(post1)
                                    .score(score1)
                                    .build(),
                                Recommend.builder()
                                    .post(post3)
                                    .score(score3)
                                    .build())
    );

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.ONGOING, PostOrder.LATEST, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals(2, response.getContent().get(0).getPostId());
    Assertions.assertEquals(1, response.getContent().get(1).getPostId());
    Assertions.assertEquals(3, response.getContent().get(2).getPostId());

    verify(recommendRepository, times(1)).deleteAll();
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(testMember.getMemberId());
    verify(postTagRepository, times(1)).findDistinctByTagIn(List.of(tag1, tag2, tag3));
    verify(postTagRepository, times(3)).findAllByPost(any());
    verify(recommendRepository, times(1)).findAll(Sort.by(Direction.DESC, "score", "post." + PostOrder.LATEST.getSortingCriteria()));
  }

  @Test
  void recommend_success_ongoing_imminentDeadline() {
    //given
    given(memberTagRepository.findAllByMember_MemberId(testMember.getMemberId()))
        .willReturn(List.of(memberTag1, memberTag2, memberTag3));
    given(postTagRepository.findDistinctByTagIn(List.of(tag1, tag2, tag3)))
        .willReturn(List.of(postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10));

    given(postTagRepository.findAllByPost(post1)).willReturn(List.of(postTag1, postTag2));
    given(postTagRepository.findAllByPost(post2)).willReturn(List.of(postTag3, postTag4));
    given(postTagRepository.findAllByPost(post3)).willReturn(List.of(postTag5));

    List<Order> orders = new ArrayList<>();
    orders.add(new Order(Direction.DESC, "score"));
    orders.add(new Order(Direction.ASC, "post." + PostOrder.IMMINENT_DEADLINE.getSortingCriteria()));
    given(recommendRepository.findAll(Sort.by(orders)))
        .willReturn(List.of(Recommend.builder()
                .post(post1)
                .score(score1)
                .build(),
            Recommend.builder()
                .post(post2)
                .score(score2)
                .build(),
            Recommend.builder()
                .post(post3)
                .score(score3)
                .build())
        );

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(
            testMember, PostStatus.ONGOING, PostOrder.IMMINENT_DEADLINE, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals(1, response.getContent().get(0).getPostId());
    Assertions.assertEquals(2, response.getContent().get(1).getPostId());
    Assertions.assertEquals(3, response.getContent().get(2).getPostId());

    verify(recommendRepository, times(1)).deleteAll();
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(testMember.getMemberId());
    verify(postTagRepository, times(1)).findDistinctByTagIn(List.of(tag1, tag2, tag3));
    verify(postTagRepository, times(3)).findAllByPost(any());
    verify(recommendRepository, times(1)).findAll(Sort.by(orders));
  }

  @Test
  void recommend_success_ongoing_mostVotes() {
    //given
    given(memberTagRepository.findAllByMember_MemberId(testMember.getMemberId()))
        .willReturn(List.of(memberTag1, memberTag2, memberTag3));
    given(postTagRepository.findDistinctByTagIn(List.of(tag1, tag2, tag3)))
        .willReturn(List.of(postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10));

    given(postTagRepository.findAllByPost(post1)).willReturn(List.of(postTag1, postTag2));
    given(postTagRepository.findAllByPost(post2)).willReturn(List.of(postTag3, postTag4));
    given(postTagRepository.findAllByPost(post3)).willReturn(List.of(postTag5));

    given(recommendRepository.findAll(
        Sort.by(Direction.DESC, "score", "post." + PostOrder.LATEST.getSortingCriteria())))
        .willReturn(List.of(Recommend.builder()
                .post(post1)
                .score(score1)
                .build(),
            Recommend.builder()
                .post(post2)
                .score(score2)
                .build(),
            Recommend.builder()
                .post(post3)
                .score(score3)
                .build())
        );

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.ONGOING, PostOrder.LATEST, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals(1, response.getContent().get(0).getPostId());
    Assertions.assertEquals(2, response.getContent().get(1).getPostId());
    Assertions.assertEquals(3, response.getContent().get(2).getPostId());

    verify(recommendRepository, times(1)).deleteAll();
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(testMember.getMemberId());
    verify(postTagRepository, times(1)).findDistinctByTagIn(List.of(tag1, tag2, tag3));
    verify(postTagRepository, times(3)).findAllByPost(any());
    verify(recommendRepository, times(1)).findAll(Sort.by(Direction.DESC, "score", "post." + PostOrder.LATEST.getSortingCriteria()));
  }

  @Test
  void recommend_success_ongoing_mostChatParticipants() {
    //given
    given(memberTagRepository.findAllByMember_MemberId(testMember.getMemberId()))
        .willReturn(List.of(memberTag1, memberTag2, memberTag3));
    given(postTagRepository.findDistinctByTagIn(List.of(tag1, tag2, tag3)))
        .willReturn(List.of(postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10));

    given(postTagRepository.findAllByPost(post1)).willReturn(List.of(postTag1, postTag2));
    given(postTagRepository.findAllByPost(post2)).willReturn(List.of(postTag3, postTag4));
    given(postTagRepository.findAllByPost(post3)).willReturn(List.of(postTag5));

    given(recommendRepository.findAll(
        Sort.by(Direction.DESC, "score", "post." + PostOrder.LATEST.getSortingCriteria())))
        .willReturn(List.of(Recommend.builder()
                .post(post2)
                .score(score2)
                .build(),
            Recommend.builder()
                .post(post1)
                .score(score1)
                .build(),
            Recommend.builder()
                .post(post3)
                .score(score3)
                .build())
        );

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.ONGOING, PostOrder.LATEST, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals(2, response.getContent().get(0).getPostId());
    Assertions.assertEquals(1, response.getContent().get(1).getPostId());
    Assertions.assertEquals(3, response.getContent().get(2).getPostId());

    verify(recommendRepository, times(1)).deleteAll();
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(testMember.getMemberId());
    verify(postTagRepository, times(1)).findDistinctByTagIn(List.of(tag1, tag2, tag3));
    verify(postTagRepository, times(3)).findAllByPost(any());
    verify(recommendRepository, times(1)).findAll(Sort.by(Direction.DESC, "score", "post." + PostOrder.LATEST.getSortingCriteria()));
  }

  @Test
  void recommend_success_completed_latest() {
    //given
    given(memberTagRepository.findAllByMember_MemberId(testMember.getMemberId()))
        .willReturn(List.of(memberTag1, memberTag2, memberTag3));
    given(postTagRepository.findDistinctByTagIn(List.of(tag1, tag2, tag3)))
        .willReturn(List.of(postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10));

    given(postTagRepository.findAllByPost(post5)).willReturn(List.of(postTag6, postTag7));
    given(postTagRepository.findAllByPost(post6)).willReturn(List.of(postTag8, postTag9));
    given(postTagRepository.findAllByPost(post7)).willReturn(List.of(postTag10));

    given(recommendRepository.findAll(
        Sort.by(Direction.DESC, "score", "post." + PostOrder.LATEST.getSortingCriteria())))
        .willReturn(List.of(Recommend.builder()
                .post(post6)
                .score(score6)
                .build(),
            Recommend.builder()
                .post(post5)
                .score(score5)
                .build(),
            Recommend.builder()
                .post(post7)
                .score(score7)
                .build())
        );

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.COMPLETED, PostOrder.LATEST, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals(6, response.getContent().get(0).getPostId());
    Assertions.assertEquals(5, response.getContent().get(1).getPostId());
    Assertions.assertEquals(7, response.getContent().get(2).getPostId());

    verify(recommendRepository, times(1)).deleteAll();
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(testMember.getMemberId());
    verify(postTagRepository, times(1)).findDistinctByTagIn(List.of(tag1, tag2, tag3));
    verify(postTagRepository, times(3)).findAllByPost(any());
    verify(recommendRepository, times(1)).findAll(Sort.by(Direction.DESC, "score", "post." + PostOrder.LATEST.getSortingCriteria()));

  }

  @Test
  void recommend_success_completed_mostVotes() {
    //given
    given(memberTagRepository.findAllByMember_MemberId(testMember.getMemberId()))
        .willReturn(List.of(memberTag1, memberTag2, memberTag3));
    given(postTagRepository.findDistinctByTagIn(List.of(tag1, tag2, tag3)))
        .willReturn(List.of(postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10));

    given(postTagRepository.findAllByPost(post5)).willReturn(List.of(postTag6, postTag7));
    given(postTagRepository.findAllByPost(post6)).willReturn(List.of(postTag8, postTag9));
    given(postTagRepository.findAllByPost(post7)).willReturn(List.of(postTag10));

    given(recommendRepository.findAll(
        Sort.by(Direction.DESC, "score", "post." + PostOrder.MOST_VOTES.getSortingCriteria())))
        .willReturn(List.of(Recommend.builder()
                .post(post5)
                .score(score5)
                .build(),
            Recommend.builder()
                .post(post6)
                .score(score6)
                .build(),
            Recommend.builder()
                .post(post7)
                .score(score7)
                .build())
        );

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.COMPLETED, PostOrder.MOST_VOTES, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals(5, response.getContent().get(0).getPostId());
    Assertions.assertEquals(6, response.getContent().get(1).getPostId());
    Assertions.assertEquals(7, response.getContent().get(2).getPostId());

    verify(recommendRepository, times(1)).deleteAll();
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(testMember.getMemberId());
    verify(postTagRepository, times(1)).findDistinctByTagIn(List.of(tag1, tag2, tag3));
    verify(postTagRepository, times(3)).findAllByPost(any());
    verify(recommendRepository, times(1)).findAll(Sort.by(Direction.DESC, "score", "post." + PostOrder.MOST_VOTES.getSortingCriteria()));
  }

  @Test
  void recommend_success_completed_mostChatParticipants() {
    //given
    given(memberTagRepository.findAllByMember_MemberId(testMember.getMemberId()))
        .willReturn(List.of(memberTag1, memberTag2, memberTag3));
    given(postTagRepository.findDistinctByTagIn(List.of(tag1, tag2, tag3)))
        .willReturn(List.of(postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10));

    given(postTagRepository.findAllByPost(post5)).willReturn(List.of(postTag6, postTag7));
    given(postTagRepository.findAllByPost(post6)).willReturn(List.of(postTag8, postTag9));
    given(postTagRepository.findAllByPost(post7)).willReturn(List.of(postTag10));

    given(recommendRepository.findAll(
        Sort.by(Direction.DESC, "score", "post." + PostOrder.MOST_CHAT_PARTICIPANTS.getSortingCriteria())))
        .willReturn(List.of(Recommend.builder()
                .post(post5)
                .score(score5)
                .build(),
            Recommend.builder()
                .post(post6)
                .score(score6)
                .build(),
            Recommend.builder()
                .post(post7)
                .score(score7)
                .build())
        );

    //when
    Page<PostListDto.Response> response =
        recommendationService.recommendBasedOnInterest(testMember, PostStatus.COMPLETED, PostOrder.MOST_CHAT_PARTICIPANTS, PageRequestCustom.of(1));

    //then
    Assertions.assertEquals(3, response.getTotalElements());
    Assertions.assertEquals(5, response.getContent().get(0).getPostId());
    Assertions.assertEquals(6, response.getContent().get(1).getPostId());
    Assertions.assertEquals(7, response.getContent().get(2).getPostId());

    verify(recommendRepository, times(1)).deleteAll();
    verify(memberTagRepository, times(1)).findAllByMember_MemberId(testMember.getMemberId());
    verify(postTagRepository, times(1)).findDistinctByTagIn(List.of(tag1, tag2, tag3));
    verify(postTagRepository, times(3)).findAllByPost(any());
    verify(recommendRepository, times(1)).findAll(Sort.by(Direction.DESC, "score", "post." + PostOrder.MOST_CHAT_PARTICIPANTS.getSortingCriteria()));
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