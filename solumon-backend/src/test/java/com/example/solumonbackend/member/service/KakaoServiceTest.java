package com.example.solumonbackend.member.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.solumonbackend.global.config.BeanConfig;
import com.example.solumonbackend.global.exception.CustomSecurityException;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.model.KakaoSignInDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
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
  @MockBean //@EnableJpaAuditing 때문에 추가
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockBean
  private MemberRepository memberRepository;
  @MockBean
  private RefreshTokenRedisRepository refreshTokenRedisRepository;
  @MockBean
  private JwtTokenProvider jwtTokenProvider;
  @Autowired // 실제로 사용할 거라 @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private KakaoService kakaoService;
  @Autowired // 가짜 서버를 띄워줌
  private MockRestServiceServer mockServer;

  @BeforeEach
  public void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  void signUpKakao_success() throws Exception {
    //given
    String code = "kakaoCode";
    String nickname = "kakao";

    Long expectedMemberId = 1L;
    Long expectedKakaoId = 123456789L;
    String expectedEmail = "sample@sample.com";

    // 가짜 서버한테 해당 주소에 요청이 오면(expect) 다음과 같은 응답을 내리라고(respond) 하는 코드
    // response는 카카오 api 응답 예시에서 가져옴
    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"token_type\":\"bearer\","
            + "\"access_token\":\"accessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"${REFRESH_TOKEN}\","
            + "\"refresh_token_expires_in\":5184000,"
            + "\"scope\":\"account_email\"}",
            MediaType.APPLICATION_JSON));

    String getUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"id\":" + expectedKakaoId +","
            + "\"connected_at\":\"2022-04-11T01:45:28Z\","
            + "\"kakao_account\":"
              + "{\"email_needs_agreement\":false,"
              + "\"is_email_valid\":true,"
              + "\"is_email_verified\":true,"
              + "\"email\":\"" + expectedEmail +
            "\"}}",
            MediaType.APPLICATION_JSON));

    given(memberRepository.existsByEmail(any())).willReturn(false);

    given(memberRepository.save(any())).willReturn(Member.builder()
        .memberId(expectedMemberId)
        .kakaoId(expectedKakaoId)
        .email(expectedEmail)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .registeredAt(LocalDateTime.now())
        .build());

    //when
    KakaoSignUpDto.Response response = kakaoService.kakaoSignUp(code, nickname);
    //then
    Assertions.assertEquals(1, response.getMemberId());
    Assertions.assertEquals("sample@sample.com" , response.getEmail());
    Assertions.assertEquals(123456789 , response.getKakaoId());
    Assertions.assertEquals(nickname, response.getNickname());
  }

  @Test
  void signUpKakao_fail_invalidCode() throws Exception {
    ///given
    String code = "kakaoCode";
    String nickname = "kakao";

    // 카카오 Api가 코드나 토큰이 올바르지 않을 시 ""를 내려줌
    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

    //when
    //then
    CustomSecurityException exception
        = Assertions.assertThrows(CustomSecurityException.class, () -> kakaoService.kakaoSignUp(code, nickname));
    Assertions.assertEquals(ErrorCode.INVALID_KAKAO_CODE, exception.getErrorCode());
  }

  @Test
  void signUpKakao_fail_noEmail() throws Exception {
    ///given
    String code = "kakaoCode";
    String nickname = "kakao";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{token_type\":\"bearer\","
            + "\"access_token\":\"accessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"${REFRESH_TOKEN}\","
            + "\"refresh_token_expires_in\":5184000}",
            MediaType.APPLICATION_JSON));

    String unlinkUrl = "https://kapi.kakao.com/v1/user/unlink";
    this.mockServer.expect(requestTo(new URI(unlinkUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"id\": 123456789}", MediaType.APPLICATION_JSON));

    //when
    //then
    MemberException exception
        = Assertions.assertThrows(MemberException.class, () -> kakaoService.kakaoSignUp(code, nickname));
    Assertions.assertEquals(ErrorCode.EMAIL_IS_REQUIRED, exception.getErrorCode());
  }

  @Test
  void signUpKakao_fail_invalidKakaoToken() throws Exception {
    ///given
    String code = "kakaoCode";
    String nickname = "kakao";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{token_type\":\"bearer\","
            + "\"access_token\":\"accessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"${REFRESH_TOKEN}\","
            + "\"refresh_token_expires_in\":5184000,"
            + "\"scope\":\"account_email\"}",
            MediaType.APPLICATION_JSON));

    String getUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

    //when
    //then
    CustomSecurityException exception
        = Assertions.assertThrows(CustomSecurityException.class, () -> kakaoService.kakaoSignUp(code, nickname));
    Assertions.assertEquals(ErrorCode.INVALID_KAKAO_TOKEN, exception.getErrorCode());
  }

  @Test
  void signUpKakao_fail_alreadyExistMember() throws Exception {
    //given
    String code = "kakaoCode";
    String nickname = "kakao";

    Long expectedKakaoId = 123456789L;
    String expectedEmail = "sample@sample.com";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"token_type\":\"bearer\","
            + "\"access_token\":\"accessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"${REFRESH_TOKEN}\","
            + "\"refresh_token_expires_in\":5184000,"
            + "\"scope\":\"account_email\"}",
            MediaType.APPLICATION_JSON));

    String getUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"id\":" + expectedKakaoId +","
            + "\"connected_at\":\"2022-04-11T01:45:28Z\","
            + "\"kakao_account\":"
              + "{\"email_needs_agreement\":false,\""
              + "is_email_valid\":true,"
              + "\"is_email_verified\":true,"
              + "\"email\":\"" + expectedEmail
            + "\"}}",
            MediaType.APPLICATION_JSON));

    given(memberRepository.existsByEmail(any())).willReturn(true);

    //when
    //then
    MemberException exception
        = Assertions.assertThrows(MemberException.class, () -> kakaoService.kakaoSignUp(code, nickname));
    Assertions.assertEquals(ErrorCode.ALREADY_EXIST_MEMBER, exception.getErrorCode());
  }

  @Test
  void signInKakao_success() throws Exception {
    String code = "kakaoCode";

    Long expectedMemberId = 1L;
    Long expectedKakaoId = 123456789L;
    String expectedEmail = "sample@sample.com";
    String expectedNickname = "kakao";

    String accessToken = "accessToken";
    String refreshToken = "refreshToken";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"token_type\":\"bearer\","
            + "\"access_token\":\"kakaoAccessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"kakaoRefreshToken\","
            + "\"refresh_token_expires_in\":5184000,"
            + "\"scope\":\"account_email\"}",
            MediaType.APPLICATION_JSON));

    String getUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"id\":" + expectedKakaoId +","
            + "\"connected_at\":\"2022-04-11T01:45:28Z\","
            + "\"kakao_account\":"
              + "{\"email_needs_agreement\":false,"
              + "\"is_email_valid\":true,"
              + "\"is_email_verified\":true,"
              + "\"email\":\"" + expectedEmail
            + "\"}}",
            MediaType.APPLICATION_JSON));

    given(memberRepository.findByEmail(any())).willReturn(Optional.of(Member.builder()
        .memberId(expectedMemberId)
        .kakaoId(expectedKakaoId)
        .email(expectedEmail)
        .nickname(expectedNickname)
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .registeredAt(LocalDateTime.now())
        .build()));

    given(jwtTokenProvider.createAccessToken(expectedEmail, List.of("ROLE_GENERAL"))).willReturn(accessToken);
    given(jwtTokenProvider.createRefreshToken(expectedEmail, List.of("ROLE_GENERAL"))).willReturn(refreshToken);

    //when
    KakaoSignInDto.Response response = kakaoService.kakaoSignIn(code);
    //then
    Assertions.assertEquals(expectedMemberId, response.getMemberId());
    Assertions.assertEquals(accessToken, response.getAccessToken());
    Assertions.assertEquals(refreshToken, response.getRefreshToken());
  }

  @Test
  void signInKakao_fail_invalidCode() throws Exception {
    String code = "kakaoCode";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

    //when
    //then
    CustomSecurityException exception
        = Assertions.assertThrows(CustomSecurityException.class, () -> kakaoService.kakaoSignIn(code));
    Assertions.assertEquals(ErrorCode.INVALID_KAKAO_CODE, exception.getErrorCode());
  }

  @Test
  void signInKakao_fail_noEmail() throws Exception {
    ///given
    String code = "kakaoCode";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{token_type\":\"bearer\","
            + "\"access_token\":\"accessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"${REFRESH_TOKEN}\","
            + "\"refresh_token_expires_in\":5184000}"
            , MediaType.APPLICATION_JSON));

    String unlinkUrl = "https://kapi.kakao.com/v1/user/unlink";
    this.mockServer.expect(requestTo(new URI(unlinkUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"id\": 123456789}", MediaType.APPLICATION_JSON));

    //when
    //then
    MemberException exception
        = Assertions.assertThrows(MemberException.class, () -> kakaoService.kakaoSignIn(code));
    Assertions.assertEquals(ErrorCode.EMAIL_IS_REQUIRED, exception.getErrorCode());
  }

  @Test
  void signInKakao_fail_invalidKakaoToken() throws Exception {
    ///given
    String code = "kakaoCode";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{token_type\":\"bearer\","
            + "\"access_token\":\"accessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"${REFRESH_TOKEN}\","
            + "\"refresh_token_expires_in\":5184000,"
            + "\"scope\":\"account_email\"}",
            MediaType.APPLICATION_JSON));

    String getUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

    //when
    //then
    CustomSecurityException exception
        = Assertions.assertThrows(CustomSecurityException.class, () -> kakaoService.kakaoSignIn(code));
    Assertions.assertEquals(ErrorCode.INVALID_KAKAO_TOKEN, exception.getErrorCode());
  }

  @Test
  void signInKakao_fail_notExistMember() throws Exception {
//given
    String code = "kakaoCode";

    Long expectedKakaoId = 123456789L;
    String expectedEmail = "sample@sample.com";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"token_type\":\"bearer\","
            + "\"access_token\":\"accessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"${REFRESH_TOKEN}\","
            + "\"refresh_token_expires_in\":5184000,"
            + "\"scope\":\"account_email\"}",
            MediaType.APPLICATION_JSON));

    String getUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"id\":" + expectedKakaoId +","
            + "\"connected_at\":\"2022-04-11T01:45:28Z\","
            + "\"kakao_account\":"
              + "{\"email_needs_agreement\":false,"
              + "\"is_email_valid\":true,"
              + "\"is_email_verified\":true,"
              + "\"email\":\"" + expectedEmail +
            "\"}}",
            MediaType.APPLICATION_JSON));

    given(memberRepository.findByEmail(any())).willReturn(Optional.empty());

    //when
    //then
    MemberException exception
        = Assertions.assertThrows(MemberException.class, () -> kakaoService.kakaoSignIn(code));
    Assertions.assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
  }

  @Test
  void signInKakao_fail_unregisteredMember() throws Exception {
    String code = "kakaoCode";

    Long expectedMemberId = 1L;
    Long expectedKakaoId = 123456789L;
    String expectedEmail = "sample@sample.com";
    String expectedNickname = "kakao";

    String getTokenUrl = "https://kauth.kakao.com/oauth/token";
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"token_type\":\"bearer\","
            + "\"access_token\":\"kakaoAccessToken\","
            + "\"expires_in\":43199,"
            + "\"refresh_token\":\"kakaoRefreshToken\","
            + "\"refresh_token_expires_in\":5184000,"
            + "\"scope\":\"account_email\"}",
            MediaType.APPLICATION_JSON));

    String getUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"id\":" + expectedKakaoId +","
            + "\"connected_at\":\"2022-04-11T01:45:28Z\","
            + "\"kakao_account\":"
             + "{\"email_needs_agreement\":false,"
              + "\"is_email_valid\":true,"
              + "\"is_email_verified\":true,"
              + "\"email\":\"" + expectedEmail
            + "\"}}",
            MediaType.APPLICATION_JSON));

    given(memberRepository.findByEmail(any())).willReturn(Optional.of(Member.builder()
        .memberId(expectedMemberId)
        .kakaoId(expectedKakaoId)
        .email(expectedEmail)
        .nickname(expectedNickname)
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .registeredAt(LocalDateTime.now().minusDays(1))
        .unregisteredAt(LocalDateTime.now())
        .build()));

    //when
    //then
    MemberException exception
        = Assertions.assertThrows(MemberException.class, () -> kakaoService.kakaoSignIn(code));
    Assertions.assertEquals(ErrorCode.UNREGISTERED_ACCOUNT, exception.getErrorCode());
  }
}