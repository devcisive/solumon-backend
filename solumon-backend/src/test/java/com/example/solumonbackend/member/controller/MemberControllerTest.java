package com.example.solumonbackend.member.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.model.KakaoSignInDto;
import com.example.solumonbackend.member.model.KakaoSignUpDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.service.KakaoService;
import com.example.solumonbackend.member.service.MemberService;
import com.example.solumonbackend.member.type.MemberRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class MemberControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private MemberService memberService;
  @Autowired
  private KakaoService kakaoService;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private RefreshTokenRedisRepository refreshTokenRedisRepository;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  @MockBean
  private JwtTokenProvider jwtTokenProvider;
  @MockBean // @EnableJpaAuditing 때문에 추가
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

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
    memberRepository.save(Member.builder()
        .email("sample@sample.com")
        .build());

    // 가짜 서버한테 해당 주소에 요청이 오면(expect) 다음과 같은 응답을 내리라고(respond) 하는 코드
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(tokenResponseWithEmail, MediaType.APPLICATION_JSON));

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));
    //when
    //then
    mockMvc.perform(get("/user/start/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("code", "kakaoCode"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isMember").value(true))
        .andExpect(jsonPath("$.kakaoAccessToken").value(kakaoAccessToken));
    this.mockServer.verify(); // expect된 요청이 실제로 요청되었는지 확인
  }

  @Test
  void startWithKakao_success_isMemberFalse() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(tokenResponseWithEmail, MediaType.APPLICATION_JSON));

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));
    //when
    //then
    mockMvc.perform(get("/user/start/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("code", "kakaoCode"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isMember").value(false))
        .andExpect(jsonPath("$.kakaoAccessToken").value(kakaoAccessToken));
    this.mockServer.verify();
  }

  @Test
  void startWithKakao_fail_invalidCode() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withNoContent());
    //when
    //then
    mockMvc.perform(get("/user/start/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("code", "kakaoCode"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_KAKAO_CODE.toString()));
    this.mockServer.verify();
  }

  @Test
  void startWithKakao_fail_noEmail() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(tokenResponseWithOutEmail, MediaType.APPLICATION_JSON));

    this.mockServer.expect(requestTo(new URI(unlinkUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(unlinkSuccessResponse, MediaType.APPLICATION_JSON));
    //when
    //then
    mockMvc.perform(get("/user/start/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("code", "kakaoCode"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.EMAIL_IS_REQUIRED.toString()));
    this.mockServer.verify();
  }

  @Test
  void startWithKakao_fail_invalidToken() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getTokenUrl)))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(tokenResponseWithEmail, MediaType.APPLICATION_JSON));

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withNoContent());
    //when
    //then
    mockMvc.perform(get("/user/start/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("code", "kakaoCode"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_KAKAO_TOKEN.toString()));
    this.mockServer.verify();
  }


  @Test
  void signUpKakao_success() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    //when
    KakaoSignUpDto.Request request = KakaoSignUpDto.Request.builder()
        .kakaoAccessToken(kakaoAccessToken)
        .nickname(nickname)
        .build();
    String json = new Gson().toJson(request);

    //then
    mockMvc.perform(post("/user/sign-up/kakao")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.kakaoId").value(kakaoId))
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.nickname").value(nickname));

    this.mockServer.verify();
  }

  @Test
  void signUpKakao_fail_invalidToken() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withNoContent());
    //when
    KakaoSignUpDto.Request request = KakaoSignUpDto.Request.builder()
        .kakaoAccessToken(kakaoAccessToken)
        .nickname(nickname)
        .build();
    String json = new Gson().toJson(request);

    //then
    mockMvc.perform(post("/user/sign-up/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_KAKAO_TOKEN.toString()));
    this.mockServer.verify();
  }

  @Test
  void signUpKakao_fail_alreadyMember() throws Exception {
    //given
    memberRepository.save(Member.builder()
        .email(email)
        .build());

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));
    //when
    KakaoSignUpDto.Request request = KakaoSignUpDto.Request.builder()
        .kakaoAccessToken(kakaoAccessToken)
        .nickname(nickname)
        .build();
    String json = new Gson().toJson(request);

    //then
    mockMvc.perform(post("/user/sign-up/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ALREADY_EXIST_MEMBER.toString()));
    this.mockServer.verify();
  }

  @Test
  void kakaoSignIn_success() throws Exception {
    //given
    memberRepository.save(Member.builder()
        .kakaoId(kakaoId)
        .email(email)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build());

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    given(jwtTokenProvider.createAccessToken(email, List.of("ROLE_GENERAL")))
        .willReturn("accessToken");
    given(jwtTokenProvider.createRefreshToken(email, List.of("ROLE_GENERAL")))
        .willReturn("refreshToken");

    //when
    KakaoSignInDto.Request request = KakaoSignInDto.Request.builder()
        .kakaoAccessToken(kakaoAccessToken)
        .build();
    String json = new Gson().toJson(request);
    //then
    mockMvc.perform(post("/user/sign-in/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isFirstLogIn").value(true))
        .andExpect(jsonPath("$.accessToken").value("accessToken"))
        .andExpect(jsonPath("$.refreshToken").value("refreshToken"));

    this.mockServer.verify();
   }

  @Test
  void kakaoSignIn_fail_invalidToken() throws Exception {
    //given
    memberRepository.save(Member.builder()
        .kakaoId(kakaoId)
        .email(email)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build());

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withNoContent());

    //when
    KakaoSignInDto.Request request = KakaoSignInDto.Request.builder()
        .kakaoAccessToken(kakaoAccessToken)
        .build();
    String json = new Gson().toJson(request);

    //then
    mockMvc.perform(post("/user/sign-in/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_KAKAO_TOKEN.toString()));
    this.mockServer.verify();
  }

  @Test
  void kakaoSignIn_fail_notFoundMember() throws Exception {
    //given
    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    //when
    KakaoSignInDto.Request request = KakaoSignInDto.Request.builder()
        .kakaoAccessToken(kakaoAccessToken)
        .build();
    String json = new Gson().toJson(request);
    //then
    mockMvc.perform(post("/user/sign-in/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_MEMBER.toString()));
    this.mockServer.verify();
  }

  @Test
  void kakaoSignIn_fail_unregisteredMember() throws Exception {
    //given
    memberRepository.save(Member.builder()
        .kakaoId(kakaoId)
        .email(email)
        .nickname(nickname)
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .registeredAt(LocalDateTime.now().minusDays(1))
        .unregisteredAt(LocalDateTime.now())
        .build());

    this.mockServer.expect(requestTo(new URI(getUserInfoUrl)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

    //when
    KakaoSignInDto.Request request = KakaoSignInDto.Request.builder()
        .kakaoAccessToken(kakaoAccessToken)
        .build();
    String json = new Gson().toJson(request);

    //then
    mockMvc.perform(post("/user/sign-in/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.UNREGISTERED_ACCOUNT.toString()));
    this.mockServer.verify();
  }
}