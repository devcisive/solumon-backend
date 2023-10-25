package com.example.solumonbackend.global.config;

import com.example.solumonbackend.global.recommend.CustomItemProcessor;
import com.example.solumonbackend.global.recommend.RepositoryItemListWriter;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.RecommendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class RecommendBatchConfig {

  private static final int CHUNK_SIZE = 1000;

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final MemberRepository memberRepository;
  private final MemberTagRepository memberTagRepository;
  private final PostTagRepository postTagRepository;
  private final RecommendRepository recommendRepository;

  // job: 배치에서 실행하는 일의 단위, 해당 job은 1시간에 한 번씩 돌릴 예정
  // step: job을 구성하는 1개 이상의 단계들, 여기에서는 chunk-oriented 방식으로 구성함
  // chunk-oriented: 데이터를 한 번에 하나씩 읽어들이는 것이 아니라 chunk size만큼 한꺼번에 읽어들인 후 처리하는 것
  @Bean
  public Job job() {
    log.info("updateRecommendJob 실행");
    return jobBuilderFactory.get("updateRecommendJob")
        .start(step())
        .build();
  }

  // step: 추천 글 업데이트
  private Step step() {
    return stepBuilderFactory.get("updateRecommendStep")
        .<Member, List<Recommend>>chunk(CHUNK_SIZE)
        .reader(updateRecommendReader())
        .processor(updateRecommendProcessor())
        .writer(updateRecommendWriterList())
        .build();
  }

  // ItemReader의 구현체, 데이터를 읽어들이는 역할을 함
  private RepositoryItemReader<Member> updateRecommendReader() {
    return new RepositoryItemReaderBuilder<Member>()
        .name("updateRecommendReader")
        .repository(memberRepository)
        .methodName("findAll")
        .sorts(Collections.singletonMap("memberId", Direction.ASC))
        .pageSize(CHUNK_SIZE)
        .maxItemCount(CHUNK_SIZE)
        .build();
  }

  // ItemProcessor은 중간에 데이터를 처리하는 역할을 함 (필수 x)
  // 로직이 복잡해서 CustomItemProcessor라는 별도 클래스로 구현
  private ItemProcessor<Member, List<Recommend>> updateRecommendProcessor() {
    return new CustomItemProcessor(memberTagRepository, postTagRepository, recommendRepository);
  }

  // RepositoryItemListWriter은 RepositoryItemWriter의 구현체, 데이터를 쓰는 역할을 함 (기본적으로 save)
  // List를 저장할 수 있게끔 override하여 구현함
  private RepositoryItemListWriter<Recommend> updateRecommendWriterList() {
    return new RepositoryItemListWriter<>(new RepositoryItemWriterBuilder<Recommend>()
        .repository(recommendRepository)
        .build());
  }

}