package com.example.solumonbackend.member.service;

import com.example.solumonbackend.global.exception.CustomSecurityException;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.CreateTokenDto;
import com.example.solumonbackend.member.model.KakaoSignInDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

  private final MemberRepository memberRepository;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;

  private final JwtTokenProvider jwtTokenProvider;
  private final RestTemplate restTemplate;

  @Value("${kakao-rest-api-key}")
  private String clientId;

  @Value("${kakao-signup-redirect-url}")
  private String signUpRedirectUrl;

  @Value("${kakao-signin-redirect-url}")
  private String signInRedirectUrl;

  @Transactional
  public KakaoSignUpDto.Response kakaoSignUp(String code, String nickname) {

    JsonElement tokenInfoJson = getKakaoTokenByCode(code, signUpRedirectUrl);
    unlinkTokenAndThrowExceptionIfNoEmail(tokenInfoJson);
    String kakaoAccessToken = tokenInfoJson.getAsJsonObject().get("access_token").getAsString();

    JsonElement userInfoJson = getUserInfoFromToken(kakaoAccessToken);
    Long kakaoIdNum = userInfoJson.getAsJsonObject().get("id").getAsLong();
    String email = userInfoJson.getAsJsonObject().get("kakao_account").getAsJsonObject().get("email").getAsString();

    checkIfNotAlreadyMember(email);

    Member member = Member.builder()
        .email(email)
        .kakaoId(kakaoIdNum)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build();
    member = memberRepository.save(member);

    return KakaoSignUpDto.Response.builder()
        .memberId(member.getMemberId())
        .kakaoId(member.getKakaoId())
        .email(member.getEmail())
        .nickname(member.getNickname())
        .build();
  }

  @Transactional
  public KakaoSignInDto.Response kakaoSignIn(String code) {

    JsonElement tokenInfoJson = getKakaoTokenByCode(code, signInRedirectUrl);
    unlinkTokenAndThrowExceptionIfNoEmail(tokenInfoJson);

    String kakaoAccessToken = tokenInfoJson.getAsJsonObject().get("access_token").getAsString();
    JsonElement userInfoJson = getUserInfoFromToken(kakaoAccessToken);

    String email = userInfoJson.getAsJsonObject().get("kakao_account")
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

    return KakaoSignInDto.Response.builder()
        .memberId(member.getMemberId())
        .isFirstLogIn(member.isFirstLogIn())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  private JsonElement getKakaoTokenByCode(String code, String redirectUri) {
    // 접속할 uri 생성
    URI uri = UriComponentsBuilder
        .fromUriString("https://kauth.kakao.com/oauth/token")
        .encode()
        .build()
        .toUri();

    // 파라미터 삽입 (contentType이 application/x-www-form-urlencoded로 지정되어 있는데 자동변환이 multivaluemap으로부터 가능함)
    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("grant_type", "authorization_code");
    requestBody.add("client_id", clientId);
    requestBody.add("redirect_uri", redirectUri);
    requestBody.add("code", code);

    // 헤더 설정 후 POST 요청 보냄
    RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
        .post(uri)
        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8")
        .body(requestBody);

    ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

    // 한 번 토큰을 발급받은 코드의 재사용 등이 금지되어 있어서 받아온 값이 null 아닌지 확인
    checkIfKakaoCodeWasValid(responseEntity.getBody());

    // String 형태로 받아온 값을 json으로 파싱
    return JsonParser.parseString(responseEntity.getBody());
  }

  // 카카오 관련 오류는 MemberException이 아닌 CustomSecurityException을 던짐
  private void checkIfKakaoCodeWasValid(String tokenInfoJson) {
    if (tokenInfoJson ==  null) {
      throw new CustomSecurityException(ErrorCode.INVALID_KAKAO_CODE);
    }
  }

  private void unlinkTokenAndThrowExceptionIfNoEmail(JsonElement tokenInfoJson) {
    // 받아온 token 항목 중에서 제출한 개인정보 범위인 scope(optional field)를 확인. 동의할 수 있는 항목이 email밖에 없기에 null과 비교
    if (tokenInfoJson.getAsJsonObject().get("scope") == null) {
      URI uri = UriComponentsBuilder
          .fromUriString("https://kapi.kakao.com/v1/user/unlink")
          .encode()
          .build()
          .toUri();

      RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
          .post(uri)
          .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8")
          .header("Authorization",
              "Bearer " + tokenInfoJson.getAsJsonObject().get("access_token").getAsString())
          .body(new LinkedMultiValueMap<>());

      restTemplate.exchange(requestEntity, String.class);

      throw new MemberException(ErrorCode.EMAIL_IS_REQUIRED);
    }
  }


  private JsonElement getUserInfoFromToken(String accessToken) {

    URI uri = UriComponentsBuilder
        .fromUriString("https://kapi.kakao.com/v2/user/me")
        .encode()
        .build()
        .toUri();

    RequestEntity<Void> requestEntity = RequestEntity
        .get(uri)
        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8")
        .header("Authorization", "Bearer " + accessToken)
        .build();

    ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

    checkIfKakaoTokenWasValid(responseEntity.getBody());

    return JsonParser.parseString(responseEntity.getBody());
  }

  private void checkIfKakaoTokenWasValid(String userInfoJson) {
    if (userInfoJson == null) {
      throw new CustomSecurityException(ErrorCode.INVALID_KAKAO_TOKEN);
    }
  }

  private void checkIfNotAlreadyMember(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new MemberException(ErrorCode.ALREADY_EXIST_MEMBER);
    }
  }

  private void checkIfNotUnregisteredMember(Member member) {
    if (member.getUnregisteredAt() != null) {
      throw new MemberException(ErrorCode.UNREGISTERED_ACCOUNT);
    }
  }
}
