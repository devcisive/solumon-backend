package com.example.solumonbackend.batch;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.RecommendRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// createdAt 때문에 @EnableJpaAuditing 주석 처리하고 했는데 더 좋은 방법을 아신다면 말씀 부탁드립니다.
@SpringBootTest
@SpringBatchTest
public class RecommendBatchStepTest {
  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private MemberTagRepository memberTagRepository;
  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private PostTagRepository postTagRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private RecommendRepository recommendRepository;

  Member testMember1, testMember2;
  Tag tag1, tag2, tag3;
  MemberTag memberTag1, memberTag2, memberTag3, memberTag4;
  Post post1, post2, post3, post4;
  PostTag postTag1, postTag2, postTag3, postTag4, postTag5;
  double score1, score2, score3;
  Recommend r1, r2, r3;

  @BeforeEach
  public void setUp() {
    // 기존 멤버: 관심 주제 태그1, 태그2, 태그3
    testMember1 = Member.builder()
        .email("sample1@gmail.com")
        .build();

    // 1시간 이내에 가입한 신규 멤버: 관심 주제 태그1
    testMember2 = Member.builder()
        .email("sample2@gmail.com")
        .build();

    memberRepository.saveAll(List.of(testMember1, testMember2));

    tag1 = Tag.builder()
        .name("태그1")
        .build();

    tag2 = Tag.builder()
        .name("태그2")
        .build();

    tag3 = Tag.builder()
        .name("태그3")
        .build();

    tagRepository.saveAll(List.of(tag1, tag2, tag3));

    memberTag1 = MemberTag.builder()
        .member(testMember1)
        .tag(tag1)
        .build();

    memberTag2 = MemberTag.builder()
        .member(testMember1)
        .tag(tag2)
        .build();

    memberTag3 = MemberTag.builder()
        .member(testMember1)
        .tag(tag3)
        .build();

    memberTag4 = MemberTag.builder()
        .member(testMember2)
        .tag(tag1)
        .build();

    memberTagRepository.saveAll(List.of(memberTag1, memberTag2, memberTag3, memberTag4));

    // 기존에 저장되어 있었음, 그대로 유지
    // 태그1, 태그2
    post1 = Post.builder()
        .title("post1")
        .member(testMember1)
        .createdAt(LocalDateTime.now().minusHours(2))
        .endAt(LocalDateTime.now().plusDays(1))
        .voteCount(10)
        .chatCount(5)
        .build();

    // 기존에 저장되어 있었음, 수정됨
    // 태그2, 태그3
    post2 = Post.builder()
        .title("post2")
        .member(testMember1)
        .createdAt(LocalDateTime.now().minusHours(2))
        .modifiedAt(LocalDateTime.now().minusMinutes(30))
        .endAt(LocalDateTime.now().plusDays(2))
        .voteCount(0)
        .chatCount(10)
        .build();

    // 기존에 저장되어 있었음, 삭제됨
    // 태그3
    post3 = Post.builder()
        .title("post3")
        .member(testMember1)
        .createdAt(LocalDateTime.now().minusHours(2))
        .modifiedAt(LocalDateTime.now().minusMinutes(30))
        .endAt(LocalDateTime.now().plusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    // 새로 생성됨
    // 태그3
    post4 = Post.builder()
        .title("post4")
        .member(testMember1)
        .createdAt(LocalDateTime.now().minusMinutes(30))
        .endAt(LocalDateTime.now().plusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    postRepository.saveAll(List.of(post1, post2, post3, post4));


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

    postTag5 = PostTag.builder()
        .post(post4)
        .tag(tag3)
        .build();

    postTagRepository.saveAll(List.of(postTag1, postTag2, postTag3, postTag4, postTag5));

    score1 = calculateCosineSimilarity(new double[] {1, 1, 1}, new double[] {1, 1, 0});
    score2 = calculateCosineSimilarity(new double[] {1, 1, 1}, new double[] {0, 1, 1});
    score3 = calculateCosineSimilarity(new double[] {1}, new double[] {1});

    r1 = Recommend.builder()
        .memberId(testMember1.getMemberId())
        .post(post1)
        .score(score1)
        .build();
    r2 = Recommend.builder()
        .memberId(testMember1.getMemberId())
        .post(post2)
        .score(score2)
        .build();
    r3 = Recommend.builder()
        .memberId(testMember1.getMemberId())
        .post(post3)
        .score(score3)
        .build();

    recommendRepository.saveAll(List.of(r1, r2, r3));
  }

  // batch test에서는 @transactional 사용 불가
  @AfterEach
  public void cleanUp() {
    recommendRepository.deleteAllInBatch();
    postTagRepository.deleteAllInBatch();
    memberTagRepository.deleteAllInBatch();
    tagRepository.deleteAllInBatch();
    postRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
  }

  /*
  (전)
  기존 멤버: post1, post2, post3

  (후)
  기존 멤버: post1, post2, post4
  신규 멤버: post1
   */
  @Test
  void updateRecommendStep_success() throws Exception {
    //given
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("updateDateTime", LocalDateTime.now().toString())
        .toJobParameters();

    // when
    JobExecution jobExecution = jobLauncherTestUtils.launchStep("updateRecommendStep", jobParameters);
    Collection actualStepExecutions = jobExecution.getStepExecutions();
    ExitStatus actualExitStatus = jobExecution.getExitStatus();

    // then
    Assertions.assertEquals(1, actualStepExecutions.size());
    Assertions.assertEquals( "COMPLETED", actualExitStatus.getExitCode());

    List<Recommend> recommendForTestMember1 = recommendRepository.findAllByMemberId(testMember1.getMemberId());
    Assertions.assertEquals(3, recommendForTestMember1.size());
    Assertions.assertEquals("post1", recommendForTestMember1.get(0).getPost().getTitle());
    Assertions.assertEquals("post2", recommendForTestMember1.get(1).getPost().getTitle());
    Assertions.assertEquals("post4", recommendForTestMember1.get(2).getPost().getTitle());;

    List<Recommend> recommendForTestMember2 = recommendRepository.findAllByMemberId(testMember2.getMemberId());
    Assertions.assertEquals(1, recommendForTestMember2.size());
    Assertions.assertEquals("post1", recommendForTestMember2.get(0).getPost().getTitle());
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
