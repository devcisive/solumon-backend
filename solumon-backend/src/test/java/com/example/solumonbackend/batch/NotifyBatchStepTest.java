package com.example.solumonbackend.batch;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.notify.entity.Notify;
import com.example.solumonbackend.notify.repository.EmitterRepository;
import com.example.solumonbackend.notify.repository.NotifyRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.repository.PostRepository;
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

// 다른 batchConfig @Configuration 어노테이션 떼어야지 step을 찾네요...
// 한 번에 한 job 밖에 인식을 못하는 것 같은데 조금 더 알아보겠습니다. 테스트 코드는 통과했습니다.
@SpringBootTest
@SpringBatchTest
public class NotifyBatchStepTest {
  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private EmitterRepository emitterRepository;
  @Autowired
  private NotifyRepository notifyRepository;

  Member testMember1, testMember2;
  Post post1, post2, post3;

  @BeforeEach
  public void setUp() {
    testMember1 = Member.builder()
        .email("sample1@gmail.com")
        .build();
    testMember2 = Member.builder()
        .email("sample2@gmail.com")
        .build();

    memberRepository.saveAll(List.of(testMember1, testMember2));

    post1 = Post.builder()
        .title("post1")
        .member(testMember1)
        .endAt(LocalDateTime.of(2023, 10, 14, 12, 0, 0))
        .build();

    post2 = Post.builder()
        .title("post2")
        .member(testMember1)
        .endAt(LocalDateTime.of(2023, 10, 14, 13, 0, 0))
        .build();

    post3 = Post.builder()
        .title("post3")
        .member(testMember2)
        .endAt(LocalDateTime.of(2023, 10, 14, 13, 0, 0))
        .build();

    postRepository.saveAll(List.of(post1, post2, post3));
  }

  @AfterEach
  public void cleanUp() {
    notifyRepository.deleteAllInBatch();
    postRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
  }



  @Test
  void voteCloseNotifyStep_success() throws Exception {
    //given
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("notifyDateTime", LocalDateTime.now().toString())
        .toJobParameters();

    // when
    JobExecution jobExecution = jobLauncherTestUtils.launchStep("voteCloseNotifyStep", jobParameters);
    Collection actualStepExecutions = jobExecution.getStepExecutions();
    ExitStatus actualExitStatus = jobExecution.getExitStatus();

    // then
    Assertions.assertEquals(1, actualStepExecutions.size());
    Assertions.assertEquals( "COMPLETED", actualExitStatus.getExitCode());

    List<Notify> notifyList = notifyRepository.findAll();
    Assertions.assertEquals(2, notifyList.size());
    Assertions.assertEquals("post2", notifyList.get(0).getPostTitle());
    Assertions.assertEquals(testMember1.getMemberId(), notifyList.get(0).getMember().getMemberId());
    Assertions.assertEquals("post3", notifyList.get(1).getPostTitle());
    Assertions.assertEquals(testMember2.getMemberId(), notifyList.get(1).getMember().getMemberId());
  }
}
