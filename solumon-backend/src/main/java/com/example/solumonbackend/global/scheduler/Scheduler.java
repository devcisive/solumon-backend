package com.example.solumonbackend.global.scheduler;

import com.example.solumonbackend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Scheduler {

  private final MemberService memberService;


  @Scheduled(cron = "0 0 0 * * *")
  public void releaseBan() {
    memberService.releaseBan();
  }
}
