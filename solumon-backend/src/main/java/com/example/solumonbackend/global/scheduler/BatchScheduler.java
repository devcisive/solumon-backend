package com.example.solumonbackend.global.scheduler;

import com.example.solumonbackend.global.config.NotifyBatchConfig;
import com.example.solumonbackend.global.config.RecommendBatchConfig;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {
  private final JobLauncher jobLauncher;
  private final RecommendBatchConfig recommendBatchConfig;
  private final NotifyBatchConfig notifyBatchConfig;

  @Scheduled(cron = "0 30 0/1 * * *")
  public void runUpdateRecommendJob() {
    Map<String, JobParameter> confMap = new HashMap<>();
    confMap.put("updateDateTime", new JobParameter(LocalDateTime.now().toString()));
    JobParameters jobParameters = new JobParameters(confMap);

    try {
      jobLauncher.run(recommendBatchConfig.job(), jobParameters);
    } catch (JobExecutionAlreadyRunningException | JobRestartException |
             JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
      log.error("updateRecommendJob failed: " + e.getMessage());
    }
  }

  @Scheduled(cron = "0 0 0/1 * * *")
  public void runVoteCloseNotifyJob() {
    Map<String, JobParameter> confMap = new HashMap<>();
    confMap.put("notifyDateTime", new JobParameter(LocalDateTime.now().toString()));
    JobParameters jobParameters = new JobParameters(confMap);

    try {
      jobLauncher.run(notifyBatchConfig.job(), jobParameters);
    } catch (JobExecutionAlreadyRunningException | JobRestartException |
             JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
      log.error("voteCloseNotifyJob failed: " + e.getMessage());
    }
  }
}
