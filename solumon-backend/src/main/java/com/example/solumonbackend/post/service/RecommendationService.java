package com.example.solumonbackend.post.service;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.repository.RecommendRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {
  private final RecommendRepository recommendRepository;

  @Transactional
  public Page<PostListDto.Response> recommendBasedOnInterest(
      Member member, PostStatus postStatus, PostOrder postOrder, Pageable pageable) {

    // batch에서 매번 업데이트 해주는 RecommendRepository에서 추천 글을 가져옴
    List<Recommend> recommendList = recommendRepository.findAllByMemberId(member.getMemberId());

    // 가져온 게시글들 중에서 진행 중 / 마감한 글만 걸러내기 -> 여기서부터는 전부 노출됨
    List<Recommend> possiblePosts = filterPostsByStatus(recommendList, postStatus);

    // 게시글을 우선 유사도가 높은 순으로 나열하고, 그 다음에 선택한 분류 기준 (최신순, 투표 마감 임박 순, 투표 참여 인원 많은 순, 채팅 참여 인원 많은 순) 적용
    // 관심 주제 태그가 최대 5개고 가중치가 같아서 코사인 유사도가 같은 항목이 많을 것으로 예상됨 -> 이 순서로 적용해도 괜찮을 것이라 판단
    List<PostListDto.Response> resultContents;

    if (PostOrder.LATEST.equals(postOrder)) {
      resultContents = possiblePosts.stream().sorted((r1, r2) -> r1.getScore().equals(r2.getScore()) ?
              r2.getPost().getCreatedAt().compareTo(r1.getPost().getCreatedAt()) : (r2.getScore().compareTo(r1.getScore())))
          .map(r -> PostListDto.Response.postToPostListResponse(r.getPost())).collect(Collectors.toList());
    } else if (PostOrder.IMMINENT_DEADLINE.equals(postOrder)) {
      resultContents = possiblePosts.stream().sorted((r1, r2) -> r1.getScore().equals(r2.getScore()) ?
              r1.getPost().getEndAt().compareTo(r2.getPost().getEndAt()) : (r2.getScore().compareTo(r1.getScore())))
          .map(r -> PostListDto.Response.postToPostListResponse(r.getPost())).collect(Collectors.toList());
    } else if (PostOrder.MOST_VOTES.equals(postOrder)) {
      resultContents = possiblePosts.stream().sorted((r1, r2) -> r1.getScore().equals(r2.getScore()) ?
              r2.getPost().getVoteCount() - r1.getPost().getVoteCount() : (r2.getScore().compareTo(r1.getScore())))
          .map(r -> PostListDto.Response.postToPostListResponse(r.getPost())).collect(Collectors.toList());
    } else if (PostOrder.MOST_CHAT_PARTICIPANTS.equals(postOrder)) {
      resultContents = possiblePosts.stream().sorted((r1, r2) -> r1.getScore().equals(r2.getScore()) ?
              r2.getPost().getChatCount() - r1.getPost().getChatCount() : (r2.getScore().compareTo(r1.getScore())))
          .map(r -> PostListDto.Response.postToPostListResponse(r.getPost())).collect(Collectors.toList());
    } else {
      throw new PostException(ErrorCode.INVALID_SORTING_CRITERIA);
    }

    return PageableExecutionUtils.getPage(resultContents, pageable, () -> (long) resultContents.size());
  }

  private List<Recommend> filterPostsByStatus(List<Recommend> recommendList, PostStatus postStatus) {
    // '진행 중' 탭일 경우 투표 마감 시간이 현재 이후인 글들만 뽑아옴
    if (PostStatus.ONGOING.equals(postStatus)) {
      return recommendList.stream().filter(
          r -> r.getPost().getEndAt().isAfter(LocalDateTime.now())).distinct().collect(Collectors.toList());
    // '마감' 탭일 경우 투표 마감 시간이 현재 이전인 글들만 뽑아옴
    } else {
      return recommendList.stream().filter(
          r -> r.getPost().getEndAt().isBefore(LocalDateTime.now())).distinct().collect(Collectors.toList());
    }
  }
}
