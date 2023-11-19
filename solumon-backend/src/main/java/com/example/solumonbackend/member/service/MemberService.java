package com.example.solumonbackend.member.service;

import static com.example.solumonbackend.global.exception.ErrorCode.ACCESS_TOKEN_NOT_FOUND;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_CORRECT_PASSWORD;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_FOUND_TAG;
import static com.example.solumonbackend.global.exception.ErrorCode.UNREGISTERED_MEMBER;

import com.example.solumonbackend.global.exception.CustomSecurityException;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.exception.TagException;
import com.example.solumonbackend.global.security.JwtTokenProvider;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.entity.Report;
import com.example.solumonbackend.member.model.CreateTokenDto;
import com.example.solumonbackend.member.model.GeneralSignInDto;
import com.example.solumonbackend.member.model.GeneralSignUpDto;
import com.example.solumonbackend.member.model.LogOutDto;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberLogDto;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.ReportDto;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.repository.ReportRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.member.type.ReportType;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.MyParticipatePostDto;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.RecommendRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRedisRepository refreshTokenRedisRepository;
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final MemberTagRepository memberTagRepository;
  private final ReportRepository reportRepository;
  private final RecommendRepository recommendRepository;
  private final PostTagRepository postTagRepository;

  @Transactional
  public GeneralSignUpDto.Response signUp(GeneralSignUpDto.Request request) {
    validateDuplicatedEmail(request.getEmail());
    validateDuplicatedNickName(request.getNickname());

    return GeneralSignUpDto.Response.memberToResponse(memberRepository.save(Member.builder()
        .kakaoId(null)
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true)
        .build()));
  }

  private void validateDuplicatedEmail(String email) {
    if (memberRepository.findByEmail(email).isPresent()) {
      throw new MemberException(ErrorCode.ALREADY_REGISTERED_EMAIL);
    }
  }

  private void validateDuplicatedNickName(String nickName) {
    if (memberRepository.findByNickname(nickName).isPresent()) {
      throw new MemberException(ErrorCode.ALREADY_REGISTERED_NICKNAME);
    }
  }

  @Transactional
  public GeneralSignInDto.Response signIn(GeneralSignInDto.Request request) {
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(NOT_CORRECT_PASSWORD);
    }
    // 탈퇴한 유저 걸러내기
    if (member.getUnregisteredAt() != null) {
      throw new MemberException(UNREGISTERED_MEMBER);
    }

    CreateTokenDto createTokenDto = CreateTokenDto.builder()
        .memberId(member.getMemberId())
        .email(member.getEmail())
        .role(member.getRole())
        .build();

    String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(),
        createTokenDto.getRoles());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(),
        createTokenDto.getRoles());

    refreshTokenRedisRepository.save(new RefreshToken(accessToken, refreshToken));

    return GeneralSignInDto.Response.builder()
        .memberId(member.getMemberId())
        .nickname(member.getNickname())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .isFirstLogIn(member.isFirstLogIn())
        .build();
  }

  @Transactional
  public LogOutDto.Response logOut(Member member, String accessToken) {
    RefreshToken refreshToken = refreshTokenRedisRepository.findByAccessToken(accessToken)
        .orElseThrow(() -> new CustomSecurityException(ACCESS_TOKEN_NOT_FOUND));

    // 해당 액서스 토큰과 연결된 리프레시 토큰을 삭제하고 그 자리에 로그아웃 기록
    refreshToken.setRefreshToken("logout");
    refreshTokenRedisRepository.save(refreshToken);

    return LogOutDto.Response.builder()
        .memberId(member.getMemberId())
        .status("로그아웃 되었습니다.")
        .build();
  }


  public MemberLogDto.Info getMyInfo(Member member) {

    // List<MemberTag>  ->  List<String>
    List<String> strMemberTags = memberTagRepository.findAllByMember_MemberId(member.getMemberId())
        .stream().map(memberTag -> memberTag.getTag().getName())
        .collect(Collectors.toList());

    return MemberLogDto.Info.memberToResponse(member, strMemberTags);
  }


  public Page<MyParticipatePostDto> getMyParticipatePosts(Member member, PostStatus postStatus,
                                                          PostParticipateType postParticipateType, PostOrder postOrder,
                                                          Pageable pageable) {
    // postRepository 와 연결된 PostRepositoryCustom 내의 메소드 호출
    return postRepository.getMyParticipatePostPages(member.getMemberId(),
        postParticipateType, postStatus, postOrder, pageable);

  }


  @Transactional
  public MemberUpdateDto.Response updateMyInfo(Member member, MemberUpdateDto.Request request) {

    // 기존 비밀번호가 불일치 시 정보수정 자체가 불가능
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(NOT_CORRECT_PASSWORD);
    }

    if (!request.getNickname().equals(member.getNickname()) &&
        memberRepository.existsByNickname(request.getNickname())) {

      throw new MemberException((ErrorCode.ALREADY_REGISTERED_NICKNAME));

    } else {
      member.setNickname(request.getNickname());
    }


    if(request.getNewPassword() != null){
      member.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    memberRepository.save(member);

    // List<MemberTag>  ->  List<String>
    List<String> strMemberTags = memberTagRepository.findAllByMember_MemberId(member.getMemberId())
        .stream().map(memberTag -> memberTag.getTag().getName())
        .collect(Collectors.toList());

    return MemberUpdateDto.Response.memberToResponse(member, strMemberTags);

  }


  @Transactional
  public WithdrawDto.Response withdrawMember(Member member, WithdrawDto.Request request) {

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(NOT_CORRECT_PASSWORD);
    }

    member.setUnregisteredAt(LocalDateTime.now());
    memberRepository.save(member);

    return WithdrawDto.Response.memberToResponse(member);
  }

  @Transactional
  public MemberInterestDto.Response registerInterest(Member member,
                                                     MemberInterestDto.Request request) {

    // 설정 전 기존의 관심주제(MemberTag) 초기화
    memberTagRepository.deleteAllByMember_MemberId(member.getMemberId());

    // 컨트롤러에서 String 으로 받아온 관심주제이름을 통해 tag 엔티티를 꺼내서 MemberTag 엔티티로 저장
    List<Tag> tags = request.getInterests().stream()
        .distinct()  // 중복된 값이 나올 시 패스
        .map(interest -> tagRepository.findByName(interest)
            .orElseThrow(() -> new TagException(NOT_FOUND_TAG, interest)))
        .collect(Collectors.toList());

    List<String> memberTags = memberTagRepository.saveAll(
        tags.stream()
            .map(tag -> MemberTag.builder().member(member).tag(tag).build())
            .collect(Collectors.toList()))
        .stream().map(memberTag -> memberTag.getTag().getName())
        .collect(Collectors.toList());

    // 설정되었거나 바뀐 관심 주제에 따라 추천 항목 교체
    recommendRepository.deleteAllByMemberId(member.getMemberId());

    List<Post> possiblePosts = postTagRepository.findDistinctByTagIn(tags)
        .stream().map(PostTag::getPost).distinct().collect(Collectors.toList());

    recommendRepository.saveAll(getCosineSimilarityOfPosts(possiblePosts, member.getMemberId(), tags, tags.size()));


    // 후에 로그인할 때 관심태그 창을 자동으로 띄우지 않게 하기 위함
    if (member.isFirstLogIn()) {
      member.setFirstLogIn(false);
      memberRepository.save(member);
    }

    return MemberInterestDto.Response.memberToResponse(member, memberTags);
  }

  private List<Recommend> getCosineSimilarityOfPosts(List<Post> possiblePosts, Long memberId,
      List<Tag> interestTags, int interestTagsSize) {
    // 모든 관심 주제 태그를 가지고 있는 가상의 게시글의 벡터
    double[] targetVector = new double[interestTagsSize];
    Arrays.fill(targetVector, 1);
    // 게시글의 벡터 -> 똑같은 배열 계속 사용하려고 밖으로 뺌
    double[] tagVector = new double[interestTagsSize];

    List<Recommend> result = new ArrayList<>();

    // 게시글 목록의 각 게시글에 대하여
    for (Post post : possiblePosts) {
      // 해당 게시글의 태그 목록을 가져옴
      List<Tag> postTags = postTagRepository.findAllByPost_PostId(post.getPostId()).stream().map(PostTag::getTag)
          .collect(Collectors.toList());
      for (int i = 0; i < interestTagsSize; i++) {
        // 각 관심 주제에 대해 해당 게시글의 태그 목록에 그 관심 주제가 포함되어 있으면 1, 없으면 0
        Tag interestTag = interestTags.get(i);
        tagVector[i] = postTags.contains(interestTag) ? 1 : 0;
      }

      // 벡터 값 산출이 끝나면 모든 관심 주제 태그를 달고 있는 글과의 코사인 유사도를 계산하여 저장함
      result.add(Recommend.builder()
          .memberId(memberId)
          .post(post)
          .score(calculateCosineSimilarity(targetVector, tagVector))
          .build());
    }
    return result;
  }

  // 코사인 유사도 구하는 공식
  private double calculateCosineSimilarity(double[] targetVector, double[] tagVector) {
    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;
    for (int i = 0; i < tagVector.length; i++) {
      dotProduct += targetVector[i] * tagVector[i];
      normA += Math.pow(targetVector[i], 2);
      normB += Math.pow(tagVector[i], 2);
    }
    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
  }


  @Transactional
  public void reportMember(Member member, ReportDto.Request request) {

    // 피신고자가 존재하는 유저인지 확인
    Member reportedMember = memberRepository.findById(request.getReportedMemberId())
        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

    if (reportedMember.getUnregisteredAt() != null) {
      throw new MemberException(UNREGISTERED_MEMBER);
    }

    // 신고유형이 OTHER인 경우 신고내용을 적어야함
    if(request.getReportType() == ReportType.OTHER && request.getReportContent() == null){
      throw new NullPointerException("신고 상세사유를 입력해주세요.");
    }

    // 이미 정지상태면 신고불가
    if (reportedMember.getBannedAt() != null) {
      throw new MemberException(ErrorCode.ALREADY_BANNED_MEMBER);
    }

    // 내가 3일안에 신고한 유저인지 확인 (가장 최근의 신고날짜를 확인)
    Optional<Report> latestReportByMe
        = reportRepository.findTopByMemberMemberIdAndReporterIdOrderByReportedAtDesc
            (reportedMember.getMemberId(),member.getMemberId());

    if (latestReportByMe.isPresent()
        && latestReportByMe.get().getReportedAt().plusDays(3).isAfter(LocalDateTime.now())) {
      throw new MemberException(ErrorCode.COOL_TIME_REPORT_MEMBER);
    }

    // 신고가 가능한 상태라면 신고
    reportRepository.save(
        Report.builder()
            .member(reportedMember)
            .reporterId(member.getMemberId())
            .reportType(request.getReportType())
            .content(request.getReportContent())
            .reportedAt(LocalDateTime.now())
            .build()
    );

    // 정지조건 충족 시 정지상태로 변환
    banMember(reportedMember);
  }


  @Transactional
  public void banMember(Member member) {

    int reportedCount = reportRepository.countByMember_MemberId(member.getMemberId());

    if (reportedCount >= 15) {      // 영구정지(ROLE_PERMANENT_BAN)
      member.setRole(MemberRole.PERMANENT_BAN);
      member.setBannedAt(LocalDateTime.now());

    } else if (reportedCount % 5 == 0) {     // 정지(BANNED)
      member.setRole(MemberRole.BANNED);
      member.setBannedAt(LocalDateTime.now());
    }

    memberRepository.save(member);
  }

  // TODO : 젠킨스 적용
  @Transactional
  public void releaseBan() {
    // 정지상태를 해제할 조건이 되는 멤버들을 뽑아온다.
    List<Member> membersToReleaseBan = memberRepository.findMembersToReleaseBannedStatus();

    // 정지해제
    for (Member member : membersToReleaseBan) {
      member.setBannedAt(null);
      member.setRole(MemberRole.GENERAL);
    }
  }

  public boolean hasInterestTags(Member member) {
    return memberTagRepository.existsByMember_MemberId(member.getMemberId());
  }

}