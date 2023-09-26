package com.example.solumonbackend.member.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.solumonbackend.global.config.BeanConfig;
import com.example.solumonbackend.global.exception.CustomSecurityException;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.model.KakaoSignInDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
import com.example.solumonbackend.member.model.StartWithKakao;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.type.MemberRole;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(KakaoService.class) // RestClient 관련 설정만 켜주고 나머지는 로드하지 않음
@Import(BeanConfig.class) // RestTemplate 생성자 주입을 위해 넣음
class KakaoServiceTest {
  @MockBean // @EnableJpaAuditing 때문에 추가
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockBean
  private MemberRepository memberRepository;
  @MockBean
  private RefreshTokenRedisRepository refreshTokenRedisRepository;
  @MockBean
  private JwtTokenProvider jwtTokenProvider;
  @Autowired
  private KakaoService kakaoService;
  @Autowired // 실제로 사용할 거라 @Autowired
  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;

  @BeforeEach
  public void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }
  // kakao URLs
  private final String getTokenUrl = "https://kauth.kakao.com/oauth/token";
  private final String unlinkUrl = "https://kapi.kakao.com/v1/user/unlink";
  private final String getUserInfoUrl = "https://kapi.kakao.com/v2/user/me";

  // kakao variables
  private final String kakaoAccessToken = "kakaoAccessToken";
  private final Long memberId = 1L;
  private final Long kakaoId = 123456789L;
  private final String email = "sample@sample.com";
  private final String nickname = "kakao";

  // kakao responses -> 카카오 api 응답 예시에서 가져옴
  private final String tokenResponseWithEmail = "{\"token_type\":\"bearer\","
      + "\"access_token\":" + kakaoAccessToken + ","
      + "\"expires_in\":43199,"
      + "\"refresh_token\":\"${REFRESH_TOKEN}\","
      + "\"refresh_token_expires_in\":5184000,"
      + "\"scope\":\"account_email\"}";

  private final String tokenResponseWithOutEmail = "{token_type\":\"bearer\","
      + "\"access_token\":" + kakaoAccessToken + ","
      + "\"expires_in\":43199,"
      + "\"refresh_token\":\"${REFRESH_TOKEN}\","
      + "\"refresh_token_expires_in\":5184000}";

  private final String unlinkSuccessResponse = "{\"id\":" + kakaoId + "}";

  private final String userInfoResponse = "{\"id\": " + kakaoId + ","
      + "\"connected_at\":\"2022-04-11T01:45:28Z\","
      + "\"kakao_account\":"
      + "{\"email_needs_agreement\":false,"
      + "\"is_email_valid\":true,"
      + "\"is_email_verified\":true,"
      + "\"email\":" + email
      + "}}";


  @Test
  void startWithKakao_success_isMemberTrue() throws Exception {
    //given
    String kakaoCode = "kakaoCode";

    // 가짜 서버한테 해당 주소에 요청이 오면(expect) 다음과 같은 응답을 내리라고(respond) 하는 코드
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(tokenResponseWithEmail, MediaType.APPLICATION_JSON));

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    given(memberRepository.existsByEmail(anyString()))
        .willReturn(true);
    //when
    StartWithKakao.Response response = kakaoService.startWithKakao(kakaoCode);
    //then
    Assertions.assertEquals(true, response.getIsMember());
    Assertions.assertEquals(kakaoAccessToken, response.getKakaoAccessToken());
    this.mockServer.verify(); // expect된 요청이 실제로 요청되었는지 확인
  }

  @Test
  void startWithKakao_success_isMemberFalse() throws Exception {
    //given
    String kakaoCode = "kakaoCode";

    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(tokenResponseWithEmail, MediaType.APPLICATION_JSON));

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    given(memberRepository.existsByEmail(anyString()))
        .willReturn(false);

    //when
    StartWithKakao.Response response = kakaoService.startWithKakao(kakaoCode);

    //then
    Assertions.assertEquals(false, response.getIsMember());
    Assertions.assertEquals(kakaoAccessToken, response.getKakaoAccessToken());
    this.mockServer.verify();
  }

  @Test
  void startWithKakao_fail_invalidCode() throws Exception {
    //given
    String kakaoCode = "kakaoCode";

    // 카카오 Api는 코드나 토큰이 올바르지 않을 시 empty body를 내려줌
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withNoContent());

    //when
    //then
    CustomSecurityException exception =
        Assertions.assertThrows(CustomSecurityException.class, () -> kakaoService.startWithKakao(kakaoCode));
    Assertions.assertEquals(ErrorCode.INVALID_KAKAO_CODE, exception.getErrorCode());
    this.mockServer.verify();
  }

  @Test
  void startWithKakao_fail_noEmail() throws Exception {
    //given
    String kakaoCode = "kakaoCode";

    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(tokenResponseWithOutEmail, MediaType.APPLICATION_JSON));

    this.mockServer.expect(requestTo(new URI(unlinkUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(unlinkSuccessResponse, MediaType.APPLICATION_JSON));

    //when
    //then
    MemberException exception =
        Assertions.assertThrows(MemberException.class, () -> kakaoService.startWithKakao(kakaoCode));
    Assertions.assertEquals(ErrorCode.EMAIL_IS_REQUIRED, exception.getErrorCode());
    this.mockServer.verify();
  }

  @Test
  void startWithKakao_fail_invalidToken() throws Exception {
    //given
    String kakaoCode = "kakaoCode";

    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(tokenResponseWithEmail, MediaType.APPLICATION_JSON));

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withNoContent());

    //when
    //then
    CustomSecurityException exception =
        Assertions.assertThrows(CustomSecurityException.class, () -> kakaoService.startWithKakao(kakaoCode));
    Assertions.assertEquals(ErrorCode.INVALID_KAKAO_TOKEN, exception.getErrorCode());
    this.mockServer.verify();
  }

  @Test
  void signUpKakao_success() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    given(memberRepository.existsByEmail(anyString()))
        .willReturn(false);

    given(memberRepository.save(any())).willReturn(Member.builder()
        .memberId(memberId)
        .kakaoId(kakaoId)
        .email(email)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .registeredAt(LocalDateTime.now())
        .build());

    //when
    KakaoSignUpDto.Response response = kakaoService.kakaoSignUp(KakaoSignUpDto.Request
        .builder()
        .kakaoAccessToken(kakaoAccessToken)
        .nickname(nickname)
        .build());
    //then
    Assertions.assertEquals(memberId, response.getMemberId());
    Assertions.assertEquals(email, response.getEmail());
    Assertions.assertEquals(kakaoId , response.getKakaoId());
    Assertions.assertEquals(nickname, response.getNickname());

    this.mockServer.verify();
    verify(memberRepository, times(1)).existsByEmail(email);
    verify(memberRepository, times(1)).save(any(Member.class));
  }

  @Test
  void signUpKakao_fail_invalidToken() throws Exception {
    ///given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withNoContent());

    //when
    //then
    CustomSecurityException exception
        = Assertions.assertThrows(CustomSecurityException.class, () -> kakaoService.kakaoSignUp(KakaoSignUpDto.Request
        .builder()
        .kakaoAccessToken(kakaoAccessToken)
        .nickname(nickname)
        .build()));
    Assertions.assertEquals(ErrorCode.INVALID_KAKAO_TOKEN, exception.getErrorCode());

    this.mockServer.verify();
  }

  @Test
  void signUpKakao_fail_alreadyMember() throws Exception {
    ///given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    given(memberRepository.existsByEmail(anyString()))
        .willReturn(true);
    //when
    //then
    MemberException exception
        = Assertions.assertThrows(MemberException.class, () -> kakaoService.kakaoSignUp(KakaoSignUpDto.Request
        .builder()
        .kakaoAccessToken(kakaoAccessToken)
        .nickname(nickname)
        .build()));
    Assertions.assertEquals(ErrorCode.ALREADY_EXIST_MEMBER, exception.getErrorCode());

    this.mockServer.verify();
    verify(memberRepository, times(1)).existsByEmail(email);
  }

  @Test
  void signInKakao_success() throws Exception {

    String accessToken = "accessToken";
    String refreshToken = "refreshToken";


    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    given(memberRepository.findByEmail(any())).willReturn(Optional.of(Member.builder()
        .memberId(memberId)
        .kakaoId(kakaoId)
        .email(email)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .registeredAt(LocalDateTime.now())
        .build()));

    given(jwtTokenProvider.createAccessToken(email, List.of("ROLE_GENERAL"))).willReturn(accessToken);
    given(jwtTokenProvider.createRefreshToken(email, List.of("ROLE_GENERAL"))).willReturn(refreshToken);

    //when
    KakaoSignInDto.Response response = kakaoService.kakaoSignIn(KakaoSignInDto.Request
        .builder()
        .kakaoAccessToken(kakaoAccessToken)
        .build());
    //then
    Assertions.assertEquals(memberId, response.getMemberId());
    Assertions.assertEquals(accessToken, response.getAccessToken());
    Assertions.assertEquals(refreshToken, response.getRefreshToken());

    this.mockServer.verify();
    verify(refreshTokenRedisRepository, times(1)).save(any(RefreshToken.class));
  }

  @Test
  void signInKakao_fail_invalidKakaoToken() throws Exception {
    ///given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withNoContent());

    //when
    //then
    CustomSecurityException exception
        = Assertions.assertThrows(CustomSecurityException.class, () -> kakaoService.kakaoSignIn(KakaoSignInDto.Request
        .builder()
        .kakaoAccessToken(kakaoAccessToken)
        .build()));
    Assertions.assertEquals(ErrorCode.INVALID_KAKAO_TOKEN, exception.getErrorCode());
    this.mockServer.verify();
  }

  @Test
  void signInKakao_fail_notExistMember() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    given(memberRepository.findByEmail(any())).willReturn(Optional.empty());

    //when
    //then
    MemberException exception
        = Assertions.assertThrows(MemberException.class, () -> kakaoService.kakaoSignIn(KakaoSignInDto.Request
        .builder()
        .kakaoAccessToken(kakaoAccessToken)
        .build()));
    Assertions.assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());

    this.mockServer.verify();
    verify(memberRepository, times(1)).findByEmail(email);
  }

  @Test
  void signInKakao_fail_unregisteredMember() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    given(memberRepository.findByEmail(any())).willReturn(Optional.of(Member.builder()
        .memberId(memberId)
        .kakaoId(kakaoId)
        .email(email)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .registeredAt(LocalDateTime.now().minusDays(1))
        .unregisteredAt(LocalDateTime.now())
        .build()));

    //when
    //then
    MemberException exception
        = Assertions.assertThrows(MemberException.class, () -> kakaoService.kakaoSignIn(KakaoSignInDto.Request
        .builder()
        .kakaoAccessToken(kakaoAccessToken)
        .build()));
    Assertions.assertEquals(ErrorCode.UNREGISTERED_ACCOUNT, exception.getErrorCode());

    this.mockServer.verify();
    verify(memberRepository, times(1)).findByEmail(email);
  }
}