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
  Post post1, post2, post3, post4, post5, post6, post7, post8;
  PostTag postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10;
  double score1, score2, score3;
  Recommend r1, r2, r3;

  @BeforeEach
  public void setUp() {
    testMember1 = Member.builder()
        .email("sample1@sample.com")
        .build();

    testMember2 = Member.builder()
        .email("sample2@sample.com")
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

    post1 = Post.builder()
        .title("post1")
        .member(testMember1)
        .createdAt(LocalDateTime.now().minusDays(2))
        .endAt(LocalDateTime.now().plusDays(1))
        .voteCount(10)
        .chatCount(5)
        .build();

    post2 = Post.builder()
        .title("post2")
        .member(testMember1)
        .createdAt(LocalDateTime.now().minusDays(1))
        .endAt(LocalDateTime.now().plusDays(2))
        .voteCount(0)
        .chatCount(10)
        .build();

    post3 = Post.builder()
        .title("post3")
        .member(testMember1)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().plusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    post4 = Post.builder()
        .title("post4")
        .member(testMember1)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().plusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    post5 = Post.builder()
        .title("post5")
        .member(testMember1)
        .createdAt(LocalDateTime.now().minusDays(2))
        .endAt(LocalDateTime.now().minusDays(1))
        .voteCount(10)
        .chatCount(5)
        .build();

    post6 = Post.builder()
        .title("post6")
        .member(testMember1)
        .createdAt(LocalDateTime.now().minusDays(1))
        .endAt(LocalDateTime.now().minusDays(2))
        .voteCount(0)
        .chatCount(10)
        .build();

    post7 = Post.builder()
        .title("post7")
        .member(testMember1)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().minusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    post8 = Post.builder()
        .title("post8")
        .member(testMember1)
        .createdAt(LocalDateTime.now())
        .endAt(LocalDateTime.now().minusDays(3))
        .voteCount(5)
        .chatCount(0)
        .build();

    postRepository.saveAll(List.of(post1, post2, post3, post4, post5, post6, post7, post8));

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

    postTagRepository.saveAll(
        List.of(postTag1, postTag2, postTag3, postTag4, postTag5, postTag6, postTag7, postTag8, postTag9, postTag10));

    double[] targetVector = new double[] {1, 1, 1};

    score1 = calculateCosineSimilarity(targetVector, new double[] {1, 1, 0});
    score2 = calculateCosineSimilarity(targetVector, new double[] {0, 1, 1});
    score3 = calculateCosineSimilarity(targetVector, new double[] {0, 0, 1});
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

  @Test
  void recommendStep1_success() throws Exception {
    //given
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
        .memberId(testMember2.getMemberId())
        .post(post3)
        .score(score3)
        .build();

    recommendRepository.saveAll(List.of(r1, r2, r3));


    JobParameters jobParameters = new JobParametersBuilder()
        .addString("updateDateTime", LocalDateTime.now().toString())
        .toJobParameters();

    // when
    JobExecution jobExecution = jobLauncherTestUtils.launchStep("deletePreviousRecommendStep", jobParameters);
    Collection actualStepExecutions = jobExecution.getStepExecutions();
    ExitStatus actualExitStatus = jobExecution.getExitStatus();

    // then
    Assertions.assertEquals(1, actualStepExecutions.size());
    Assertions.assertEquals( "COMPLETED", actualExitStatus.getExitCode());
    Assertions.assertEquals(0, recommendRepository.findAll().size());
  }

  @Test
  void recommendStep2_success() throws Exception {
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
    Assertions.assertEquals(6, recommendForTestMember1.size());
    Assertions.assertTrue(recommendForTestMember1.get(0).getScore() > recommendForTestMember1.get(2).getScore());
    Assertions.assertTrue(recommendForTestMember1.get(1).getScore() > recommendForTestMember1.get(2).getScore());
    Assertions.assertEquals(recommendForTestMember1.get(0).getScore(), recommendForTestMember1.get(1).getScore());
    Assertions.assertTrue(recommendForTestMember1.get(3).getScore() > recommendForTestMember1.get(5).getScore());
    Assertions.assertTrue(recommendForTestMember1.get(4).getScore() > recommendForTestMember1.get(5).getScore());
    Assertions.assertEquals(recommendForTestMember1.get(3).getScore(), recommendForTestMember1.get(4).getScore());

    List<Recommend> recommendForTestMember2 = recommendRepository.findAllByMemberId(testMember2.getMemberId());
    Assertions.assertEquals(2, recommendForTestMember2.size());
    Assertions.assertEquals(recommendForTestMember1.get(0).getScore(), recommendForTestMember1.get(1).getScore());
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
