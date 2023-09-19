package com.example.solumonbackend.member.service;

import com.example.solumonbackend.member.repository.MemberRepository;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {
  private final MemberRepository memberRepository;

  @Value("${kakao-rest-api-key}")
  private String clientId;
  private String redirectUri = "http://localhost:8080/users/sign-in/kakao"
  public String getKakaoToken(String code) {
    String requestUrl = "https://kauth.kakao.com/oauth/token";
    try {
      URL url = new URL(requestUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return "";
  }
}
