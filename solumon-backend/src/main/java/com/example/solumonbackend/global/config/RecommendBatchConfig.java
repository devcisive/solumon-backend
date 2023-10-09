package com.example.solumonbackend.global.config;

import com.example.solumonbackend.global.recommend.CustomItemProcessor;
import com.example.solumonbackend.global.recommend.JpaItemListWriter;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.RecommendRepository;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RecommendBatchConfig {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final EntityManagerFactory entityManagerFactory;
  private final MemberTagRepository memberTagRepository;
  private final PostTagRepository postTagRepository;
  private final RecommendRepository recommendRepository;
  private static final int CHUNK_SIZE = 1000;

  // job: 배치에서 실행하는 일의 단위
  // step: job을 구성하는 1개 이상의 단계들, 여기에서는 chunk-oriented 방식으로 구성함
  // chunk-oriented: 데이터를 한 번에 하나씩 읽어들이는 것이 아니라 chunk size만큼 한꺼번에 읽어들인 후 처리하는 것
  @Bean
  public Job job() {
    return jobBuilderFactory.get("updateRecommendJob")
        .start(step1())
        .next(step2())
        .build();
  }

  // step1: 기존 테이블을 삭제함 (JpaItemWriter은 delete를 지원하지 않아서 나누었음)
  @Bean
  public Step step1() {
    return stepBuilderFactory.get("deletePreviousRecommendStep")
        .<Recommend, Recommend>chunk(CHUNK_SIZE)
        .reader(deletePreviousRecommendReader())
        .writer(deletePreviousRecommendWriter())
        .build();
  }

  // itemreader: 데이터를 불러오는 역할, JpaPagingItemReader은 ItemReader의 구현체
  private JpaPagingItemReader<Recommend> deletePreviousRecommendReader() {
    return new JpaPagingItemReaderBuilder<Recommend>()
        .name("deletePreviousRecommendReader")
        .entityManagerFactory(entityManagerFactory)
        .pageSize(CHUNK_SIZE)
        .queryString("select r from Recommend r")
        .build();
  }

  // itemwriter: 데이터를 쓰거나 수정하는 역할, RepositoryItemWriter은 ItemWriter의 구현체
  private RepositoryItemWriter<Recommend> deletePreviousRecommendWriter() {
    return new RepositoryItemWriterBuilder<Recommend>()
        .repository(recommendRepository)
        .methodName("delete")
        .build();
  }

  // step2: 추천 글 업데이트
  private Step step2() {
    return stepBuilderFactory.get("updateRecommendStep")
        .<Member, List<Recommend>>chunk(CHUNK_SIZE)
        .reader(updateRecommendReader())
        .processor(updateRecommendProcessor())
        .writer(updateRecommendWriterList())
        .build();
  }

  // 마찬가지로 ItemReader의 구현체
  private JpaPagingItemReader<Member> updateRecommendReader() {
    return new JpaPagingItemReaderBuilder<Member>()
        .name("updateRecommendReader")
        .entityManagerFactory(entityManagerFactory)
        .pageSize(CHUNK_SIZE)
        .queryString("select m from Member m")
        .build();
  }

  // ItemProcessor은 중간에 데이터를 처리하는 역할을 함 (필수 x)
  // 로직이 복잡해서 CustomItemProcessor라는 별도 클래스로 구현
  private ItemProcessor<Member, List<Recommend>> updateRecommendProcessor() {
    return new CustomItemProcessor(memberTagRepository, postTagRepository);
  }

  // JpaItemListWriter은 JpaItemWriter의 구현체
  // List를 저장할 수 있게끔 override하여 구현함
  private JpaItemListWriter<Recommend> updateRecommendWriterList() {
    JpaItemWriter<Recommend> jpaItemWriter = new JpaItemWriter<>();
    jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
    return new JpaItemListWriter<>(jpaItemWriter);
  }
}