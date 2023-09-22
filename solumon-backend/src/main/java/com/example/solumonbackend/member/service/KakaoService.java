package com.example.solumonbackend.member.service;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.KakaoLogInDto;
import com.example.solumonbackend.member.model.KakaoLogOutDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
import com.example.solumonbackend.member.model.KakaoTokenUpdateDto;
import com.example.solumonbackend.member.model.MemberDetail;
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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

  private final MemberRepository memberRepository;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;

  @Value("${kakao-rest-api-key}")
  private String clientId;
  @Value("${kakao-admin-key}")
  private String adminKey;

  public KakaoSignUpDto.Response kakaoSignUp(String code, String nickname) {

    JsonElement tokenInfoJson = getKakaoTokenByCode(code, "http://localhost:8080/user/sign-up/kakao");
    unlinkTokenAndThrowExceptionIfNoEmail(tokenInfoJson);

    String accessToken = tokenInfoJson.getAsJsonObject().get("access_token").getAsString();
    String refreshToken = tokenInfoJson.getAsJsonObject().get("refresh_token").getAsString();

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

    refreshTokenRedisRepository.save(RefreshToken.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build());

    return KakaoSignUpDto.Response.builder()
        .memberId(member.getMemberId())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  public KakaoLogInDto.Response kakaoLogIn(String code) {
    JsonElement tokenInfoJson = getKakaoTokenByCode(code, "http://localhost:8080/user/sign-up/kakao");
    String accessToken = tokenInfoJson.getAsJsonObject().get("access_token").getAsString();
    String refreshToken = tokenInfoJson.getAsJsonObject().get("refresh_token").getAsString();

    String email = getUserInfoFromToken(accessToken)
        .getAsJsonObject().get("kakao_account")
        .getAsJsonObject().get("email").getAsString();

    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

    refreshTokenRedisRepository.save(RefreshToken.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build());

    return KakaoLogInDto.Response.builder()
        .memberId(member.getMemberId())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  public KakaoTokenUpdateDto.Response kakaoTokenUpdate(MemberDetail memberDetail, String oldAccessToken) {
    RefreshToken refreshToken = refreshTokenRedisRepository.findByAccessToken(oldAccessToken)
        .orElseThrow(() -> new MemberException(ErrorCode.ACCESS_TOKEN_NOT_FOUND));

    JsonElement tokenInfoJson = getKakaoTokenByRefresh(refreshToken.getRefreshToken());
    Set<String> tokenInfoJsonKeySet = tokenInfoJson.getAsJsonObject().keySet();
    String newAccessToken = tokenInfoJson.getAsJsonObject().get("access_token").getAsString();
    String newRefreshToken = "";

    if (tokenInfoJsonKeySet.contains("refresh_token")) {
      newRefreshToken = tokenInfoJson.getAsJsonObject().get("refresh_token").getAsString();
    } else {
      newRefreshToken = refreshToken.getRefreshToken();
    }

    refreshTokenRedisRepository.save(RefreshToken.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build());
    refreshTokenRedisRepository.deleteByAccessToken(oldAccessToken);

    return KakaoTokenUpdateDto.Response.builder()
        .memberId(memberDetail.getMember().getMemberId())
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  public KakaoLogOutDto.Response kakaoLogOut(MemberDetail memberDetail) {
    try {
      HttpURLConnection connection
          = (HttpURLConnection) new URL("https://kapi.kakao.com/v1/user/logout").openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setRequestProperty("Authorization", "KakaoAK " + adminKey);

      if (connection.getResponseCode() != 200) {
        return KakaoLogOutDto.Response.builder()
            .memberId(memberDetail.getMember().getMemberId())
            .status("로그아웃에 실패했습니다.")
            .build();
      }
    }  catch (IOException e) {
      log.error("kakaoLogout에서 IO exception 발생");
    }

    return KakaoLogOutDto.Response.builder()
        .memberId(memberDetail.getMember().getMemberId())
        .status("로그아웃에 성공하였습니다.")
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

  public JsonElement getKakaoTokenByRefresh(String refreshToken) {
    try {
      HttpURLConnection connection
          = (HttpURLConnection) new URL("https://kauth.kakao.com/oauth/token").openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

      if (connection.getResponseCode() != 200) {
        throw new MemberException(ErrorCode.REFRESH_TOKEN_EXPIRED);
      }

      try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
        StringBuilder sb = new StringBuilder();
        sb.append("grant_type=refresh_token")
            .append("&client_id=").append(clientId)
            .append("&refresh_token=").append(refreshToken);

        bufferedWriter.write(sb.toString());
        bufferedWriter.flush();
        return readJson(connection);
      }
    } catch (IOException e) {
      log.error("getKakaoTokenByRefresh에서 IOException 발생: " + e.getMessage());
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
}
