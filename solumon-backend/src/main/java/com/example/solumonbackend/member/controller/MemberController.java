package com.example.solumonbackend.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public class MemberController {
  @GetMapping
  public void kakaoSignup() {
    System.out.println("kakao");
  }
}
