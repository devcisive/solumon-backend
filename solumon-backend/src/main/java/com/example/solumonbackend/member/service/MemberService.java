package com.example.solumonbackend.member.service;

import static com.example.solumonbackend.global.exception.ErrorCode.NOT_CORRECT_PASSWORD;
import static com.example.solumonbackend.global.exception.ErrorCode.NOT_FOUND_TAG;

import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.model.MemberInterestDto;
import com.example.solumonbackend.member.model.MemberLogDto;
import com.example.solumonbackend.member.model.MemberUpdateDto;
import com.example.solumonbackend.member.model.WithdrawDto;
import com.example.solumonbackend.member.repository.BanRepository;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.MyActivePostDto;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import com.example.solumonbackend.post.type.PostActiveType;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostState;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  private final BanRepository banRepository;
  private final TagRepository tagRepository;
  private final MemberTagRepository memberTagRepository;
  private final PostRepository postRepository;


  /**
   * (#6) 내 정보 조회
   *
   * @param member
   * @return
   */
  public MemberLogDto.Info getMyInfo(Member member) {

    List<MemberTag> memberTags = memberTagRepository.findAllByMember(member);
    List<String> strMemberTags = this.toStrMemberTags(memberTags);

    return MemberLogDto.Info.of(member, strMemberTags);
  }


  /**
   * (#6) 내 활동한 게시글목록 조회 (작성한 글/ 채팅참여한 글/ 투표한 참여글)
   *
   * @param member
   * @return
   */
  public List<MyActivePostDto> getMyActivePosts(Member member, PostState postState,
      PostActiveType postActiveType, PostOrder postOrder) {

    return postRepository.getMyActivePosts(member.getMemberId(), postState, postActiveType,
        postOrder);

  }


  /**
   * (#6) 내 정보 수정하기
   *
   * @param member
   * @param request
   * @return
   */
  public MemberUpdateDto.Response updateMyInfo(
      Member member,
      MemberUpdateDto.Request request) {

    //기존 비밀번호가 일치해야 정보수정 가능
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(NOT_CORRECT_PASSWORD);
    }

    String newPassword1 = request.getNewPassword1();
    if (newPassword1 != null) {
      if (!newPassword1.equals(request.getNewPassword2())) {
        throw new MemberException(NOT_CORRECT_PASSWORD);
      }
      String newEncodingPassword = passwordEncoder.encode(request.getPassword());
      member.setPassword(newEncodingPassword);
    }

    member.setNickname(request.getNickname());
    memberRepository.save(member);

    List<MemberTag> memberTags = memberTagRepository.findAllByMember(member);
    List<String> strMemberTags = this.toStrMemberTags(memberTags);

    return MemberUpdateDto.Response.of(member, strMemberTags);

  }


  /**
   * #6 회원탈퇴
   *
   * @param member
   * @param request
   */
  public WithdrawDto.Response withdrawMember(Member member, WithdrawDto.Request request) {

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(NOT_CORRECT_PASSWORD);
    }

    member.setUnregisteredAt(LocalDateTime.now());
    memberRepository.save(member);

    return WithdrawDto.Response.of(member);
  }


  /**
   * (#8) 관심주제 설정
   *
   * @param member
   * @param request
   * @return
   */
  public MemberInterestDto.Response registerInterest(Member member,
      MemberInterestDto.Request request) {

    memberTagRepository.deleteAllByMember(member);

    // 컨트롤러에서 String 으로 받아온 태그이름을 통해 tag 엔티티를 꺼내서 MemberTag 로 저장
    List<Tag> tags = new ArrayList<>();
    for (String interest : request.getInterests()) {
      tags.add(tagRepository.findByName(interest)
          .orElseThrow(() -> new RuntimeException(NOT_FOUND_TAG.getDescription()))); //임시로 해놓은 상태
    }

    for (Tag tag : tags) {
      memberTagRepository.save(new MemberTag(member, tag));
    }

    List<MemberTag> memberTags = memberTagRepository.findAllByMember(member);

    List<String> strMemberTags = this.toStrMemberTags(memberTags);
    return MemberInterestDto.Response.of(member, strMemberTags);
  }

  // 태그용 메소드(임시)
  public List<String> toStrMemberTags(List<MemberTag> memberTags) {
    List<String> interests = new ArrayList<>();

    for (MemberTag memberTag : memberTags) {
      interests.add(memberTag.getTag().getName());
    }

    return interests;
  }


}


