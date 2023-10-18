package com.example.solumonbackend.global.config;

import com.example.solumonbackend.notify.repository.EmitterRepository;
import com.example.solumonbackend.notify.repository.NotifyRepository;
import com.example.solumonbackend.notify.service.NotifyService;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort.Direction;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class NotifyBatchConfig {

  private static final int CHUNK_SIZE = 1000;
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final PostRepository postRepository;
  private final EmitterRepository emitterRepository;
  private final NotifyRepository notifyRepository;

  @Bean
  public Job job() {
    return jobBuilderFactory.get("voteCloseNotifyJob")
        .start(step())
        .build();
  }

  private Step step() {
    return stepBuilderFactory.get("voteCloseNotifyStep")
        .<Post, Post>chunk(CHUNK_SIZE)
        .reader(voteCloseNotifyReader())
        .writer(voteCloseNotifyWriter())
        .build();
  }

  // 정각마다 해당 배치를 돌릴 예정이라 해당 시간에서 endAt 기준 시간을 추출하는 코드
  private RepositoryItemReader<Post> voteCloseNotifyReader() {
    LocalDateTime cur = LocalDateTime.now();
    LocalDateTime now
        = LocalDateTime.of(cur.getYear(), cur.getMonth(), cur.getDayOfMonth(), cur.getHour(), 0, 0);
    return new RepositoryItemReaderBuilder<Post>()
        .name("voteCloseNotifyReader")
        .repository(postRepository)
        .methodName("findByEndAtIs")
        .arguments(List.of(now))
        .sorts(Collections.singletonMap("postId", Direction.ASC))
        .pageSize(CHUNK_SIZE)
        .maxItemCount(CHUNK_SIZE)
        .build();
  }

  // 기존 서비스에서 코드를 가져오는 형태의 ItemWriter. 위에서 넘겨받은 Post만 인자값으로 받을 수 있습니다.
  private ItemWriter<Post> voteCloseNotifyWriter() {
    ItemWriterAdapter<Post> writer = new ItemWriterAdapter<>();
    writer.setTargetObject(new NotifyService(emitterRepository, notifyRepository, postRepository));
    writer.setTargetMethod("sendForBatch");
    return writer;
  }
}
