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
import com.example.solumonbackend.member.model.KakaoTokenInfoDto;
import com.example.solumonbackend.member.model.KakaoUserInfoDto;
import com.example.solumonbackend.member.model.StartWithKakao;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.type.MemberRole;
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

  @Value("${kakao.rest-api-key}")
  private String clientId;
  @Value("${kakao.redirect-url}")
  private String redirectUrl;
  @Value("${kakao.get-token-url}")
  private String getTokenUrl;
  @Value("${kakao.unlink-url}")
  private String unlinkUrl;
  @Value("${kakao.get-info-url}")
  private String getInfoUlr;

  @Transactional(readOnly = true)
  public StartWithKakao.Response startWithKakao(String code) {

    // uri로부터 받아온 인가 코드로 json 형태의 토큰 정보 가져옴
    KakaoTokenInfoDto kakaoTokenInfoDto = getKakaoTokenByCode(code, redirectUrl);
    // 만일 토큰 정보 중 동의 항목에 이메일이 없으면 토큰 무효화, exception 던짐
    unlinkTokenAndThrowExceptionIfNoEmail(kakaoTokenInfoDto);

    // 토큰 정보로부터 카카오 access token만 가져옴
    String kakaoAccessToken = kakaoTokenInfoDto.getAccessToken();

    // access token 안에 담긴 json 형태의 사용자 정보 가져옴
    KakaoUserInfoDto kakaoUserInfoDto = getUserInfoFromToken(kakaoAccessToken);
    // 사용자 정보로부터 이메일 추출
    String email = kakaoUserInfoDto.getKakaoAccount().getEmail();

    return StartWithKakao.Response.builder()
            .isMember(memberRepository.existsByEmail(email)) // DB에 저장된 회원인지 아닌지 결과값
            .kakaoAccessToken(kakaoAccessToken) // 추출한 카카오 access token
            .build();
  }

  @Transactional
  public KakaoSignUpDto.Response kakaoSignUp(KakaoSignUpDto.Request request) {

    // 카카오 access token으로부터 json 형태의 사용자 정보 가져옴
    KakaoUserInfoDto kakaoUserInfoDto = getUserInfoFromToken(request.getKakaoAccessToken());
    // 사용자 정보로부터 카카오ID 추출
    Long kakaoIdNum = kakaoUserInfoDto.getKakaoId();
    // 사용자 정보로부터 이메일 추출
    String email = kakaoUserInfoDto.getKakaoAccount().getEmail();

    // 이미 회원이면 throw exception. 리다이렉션 아닌 주소로 들어올 수도 있어서 추가
    checkIfNotAlreadyMember(email);

    // 닉네임 중복 확인
    checkIfNotDuplicatedNickname(request.getNickname());

    Member member = Member.builder()
        .email(email)
        .kakaoId(kakaoIdNum)
        .nickname(request.getNickname()) // 요청에 보낸 사용자 지정 닉네임
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
  public KakaoSignInDto.Response kakaoSignIn(KakaoSignInDto.Request request) {

    // 카카오 액서스 토큰으로부터 json 형태의 사용자 정보 가져옴
    KakaoUserInfoDto kakaoUserInfoDto = getUserInfoFromToken(request.getKakaoAccessToken());

    // 사용자 정보로부터 이메일 추출
    String email = kakaoUserInfoDto.getKakaoAccount().getEmail();

    // 이메일을 기준으로 회원 엔티티 가져옴
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

    // 탈퇴한 회원 아닌지 검증
    checkIfNotUnregisteredMember(member);

    // 사용자 아이디, 이메일, 역할 담은 토큰 생성 dto
    CreateTokenDto createTokenDto = CreateTokenDto.builder()
        .memberId(member.getMemberId())
        .email(member.getEmail())
        .role(member.getRole())
        .build();

    // 액서스 토큰과 리프레시 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), createTokenDto.getRoles());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), createTokenDto.getRoles());

    // 레디스에 액서스 토큰과 리프레시 토큰 저장
    refreshTokenRedisRepository.save(new RefreshToken(accessToken, refreshToken));

    return KakaoSignInDto.Response.builder()
        .memberId(member.getMemberId())
        .isFirstLogIn(member.isFirstLogIn())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  private KakaoTokenInfoDto getKakaoTokenByCode(String code, String redirectUri) {
    // 접속할 uri 생성
    URI uri = UriComponentsBuilder
        .fromUriString(getTokenUrl)
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
        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8")
        .body(requestBody);

    ResponseEntity<KakaoTokenInfoDto> responseEntity = restTemplate.exchange(requestEntity, KakaoTokenInfoDto.class);

    // 한 번 토큰을 발급받은 코드의 재사용 등이 금지되어 있어서 받아온 값이 null 아닌지 확인
    checkIfKakaoCodeWasValid(responseEntity.getBody());

    // String 형태로 받아온 값을 json으로 파싱
    return responseEntity.getBody();
  }

  // 카카오 관련 오류는 MemberException이 아닌 CustomSecurityException을 던짐
  private void checkIfKakaoCodeWasValid(KakaoTokenInfoDto kakaoTokenInfoDto) {
    if (kakaoTokenInfoDto ==  null) {
      throw new CustomSecurityException(ErrorCode.INVALID_KAKAO_CODE);
    }
  }

  private void unlinkTokenAndThrowExceptionIfNoEmail(KakaoTokenInfoDto kakaoTokenInfoDto) {
    // 받아온 token 항목 중에서 제출한 개인정보 범위인 scope(optional field)를 확인. 동의할 수 있는 항목이 email밖에 없기에 null과 비교
    if (kakaoTokenInfoDto.getScope() == null) {
      URI uri = UriComponentsBuilder
          .fromUriString(unlinkUrl)
          .encode()
          .build()
          .toUri();

      RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
          .post(uri)
          .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8")
          .header("Authorization",
              "Bearer " + kakaoTokenInfoDto.getAccessToken())
          .body(new LinkedMultiValueMap<>());

      restTemplate.exchange(requestEntity, String.class);

      throw new MemberException(ErrorCode.EMAIL_IS_REQUIRED);
    }
  }

  private KakaoUserInfoDto getUserInfoFromToken(String accessToken) {

    URI uri = UriComponentsBuilder
        .fromUriString(getInfoUlr)
        .encode()
        .build()
        .toUri();

    RequestEntity<Void> requestEntity = RequestEntity
        .get(uri)
        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8")
        .header("Authorization", "Bearer " + accessToken)
        .build();

    ResponseEntity<KakaoUserInfoDto> responseEntity = restTemplate.exchange(requestEntity, KakaoUserInfoDto.class);

    checkIfKakaoTokenWasValid(responseEntity.getBody());

    return responseEntity.getBody();
  }

  private void checkIfKakaoTokenWasValid(KakaoUserInfoDto kakaoUserInfoDto) {
    if (kakaoUserInfoDto == null) {
      throw new CustomSecurityException(ErrorCode.INVALID_KAKAO_TOKEN);
    }
  }

  private void checkIfNotAlreadyMember(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new MemberException(ErrorCode.ALREADY_EXIST_MEMBER);
    }
  }

  private void checkIfNotDuplicatedNickname(String nickname) {
    if (memberRepository.existsByNickname(nickname)) {
      throw new MemberException(ErrorCode.ALREADY_EXIST_USERNAME);
    }
  }

  private void checkIfNotUnregisteredMember(Member member) {
    if (member.getUnregisteredAt() != null) {
      throw new MemberException(ErrorCode.UNREGISTERED_ACCOUNT);
    }
  }
}
