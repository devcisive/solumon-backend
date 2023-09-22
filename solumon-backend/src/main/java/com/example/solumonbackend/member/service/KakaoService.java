package com.example.solumonbackend.member.service;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.CreateTokenDto;
import com.example.solumonbackend.member.model.KakaoLogInDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

  private final MemberRepository memberRepository;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;

  private final JwtTokenProvider jwtTokenProvider;

  @Value("${kakao-rest-api-key}")
  private String clientId;

  @Transactional
  public KakaoSignUpDto.Response kakaoSignUp(String code, String nickname) {

    JsonElement tokenInfoJson = getKakaoTokenByCode(code, "http://localhost:8080/user/sign-up/kakao");
    unlinkTokenAndThrowExceptionIfNoEmail(tokenInfoJson);

    String accessToken = tokenInfoJson.getAsJsonObject().get("access_token").getAsString();
    JsonElement userInfoJson = getUserInfoFromToken(accessToken);
    Long kakaoIdNum = userInfoJson.getAsJsonObject().get("id").getAsLong();
    String email = userInfoJson.getAsJsonObject().get("kakao_account").getAsJsonObject().get("email").getAsString();

    Member member = Member.builder()
        .email(email)
        .kakaoId(kakaoIdNum)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .build();
    memberRepository.save(member);

    return KakaoSignUpDto.Response.builder()
        .memberId(member.getMemberId())
        .kakaoId(member.getKakaoId())
        .email(member.getEmail())
        .nickname(member.getNickname())
        .build();
  }

  @Transactional
  public KakaoLogInDto.Response kakaoLogIn(String code) {
    JsonElement tokenInfoJson = getKakaoTokenByCode(code, "http://localhost:8080/user/sign-up/kakao");
    String kakaoAccessToken = tokenInfoJson.getAsJsonObject().get("access_token").getAsString();

    String email = getUserInfoFromToken(kakaoAccessToken)
        .getAsJsonObject().get("kakao_account")
        .getAsJsonObject().get("email").getAsString();

    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

    checkIfNotUnregisteredMember(member);

    CreateTokenDto createTokenDto = CreateTokenDto.builder()
        .memberId(member.getMemberId())
        .email(member.getEmail())
        .role(member.getRole())
        .build();

    String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), createTokenDto.getRoles());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), createTokenDto.getRoles());

    refreshTokenRedisRepository.save(new RefreshToken(accessToken, refreshToken));

    return KakaoLogInDto.Response.builder()
        .memberId(member.getMemberId())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }


  public JsonElement getKakaoTokenByCode(String code, String redirectUri) {
    try {
      HttpURLConnection connection
          = (HttpURLConnection) new URL("https://kauth.kakao.com/oauth/token").openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

      try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
        StringBuilder sb = new StringBuilder();
        sb.append("grant_type=authorization_code")
            .append("&client_id=").append(clientId)
            .append("&redirect_uri=").append(redirectUri)
            .append("&code=").append(code);

        bufferedWriter.write(sb.toString());
        bufferedWriter.flush();
        return readJson(connection);
      }
    } catch (IOException e) {
      log.error("getKakaoTokenByCode에서 IOException 발생: " + e.getMessage());
    }
    return null;
  }

  private void unlinkTokenAndThrowExceptionIfNoEmail(JsonElement tokenInfoJson) {

    if (tokenInfoJson.getAsJsonObject().get("scope") == null) {
      try {
        HttpURLConnection unlinkConnection
            = (HttpURLConnection) new URL("https://kapi.kakao.com/v1/user/unlink").openConnection();
        unlinkConnection.setRequestMethod("POST");
        unlinkConnection.setDoOutput(true);
        unlinkConnection.setRequestProperty("Authorization",
            "Bearer " + tokenInfoJson.getAsJsonObject().get("access_token").getAsString());
        throw new MemberException(ErrorCode.EMAIL_IS_REQUIRED);
      } catch (IOException e) {
        log.error("unlinkTokenAndThrowExceptionIfNoEmail에서 IOException 발생: " + e.getMessage());
      }
    }
  }

  private JsonElement readJson(HttpURLConnection connection) {
    StringBuilder result = new StringBuilder();
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String line = "";
      while ((line = bufferedReader.readLine()) != null) {
        result.append(line);
      }
    } catch (IOException e) {
      log.error("readJson에서 IOException 발생: " + e.getMessage());
    }
    return JsonParser.parseString(result.toString());
  }

  private JsonElement getUserInfoFromToken(String accessToken) {
    try {
      HttpURLConnection connection
          = (HttpURLConnection) new URL("https://kapi.kakao.com/v2/user/me").openConnection();
      connection.setRequestMethod("GET");
      connection.setDoOutput(true);
      connection.setRequestProperty("Authorization", "Bearer " + accessToken);
      connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

      return readJson(connection);
    } catch (IOException e) {
      log.error("getUserInfoFromToken에서 IOException 발생: " + e.getMessage());
    }
    return null;
  }

  private void checkIfNotUnregisteredMember(Member member) {
    if (member.getUnregisteredAt() != null) {
      throw new MemberException(ErrorCode.UNREGISTERED_ACCOUNT);
    }
  }
}
