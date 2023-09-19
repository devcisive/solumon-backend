package com.example.solumonbackend.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("user")

public class MemberController {
  @GetMapping
  public void kakaoSignup() {
    System.out.println("kakao");
  }

// mergetest
}
