package com.example.solumonbackend.post.service;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.RecommendRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {
  private final MemberTagRepository memberTagRepository;
  private final PostTagRepository postTagRepository;
  private final RecommendRepository recommendRepository;

  /*
  코사인 유사도에 대해서:

  기본 개념은 게시글 한 개를 벡터(여기에서는 일종의 배열)로 표현하는 것으로 시작합니다.
  유저가 선택한 관심 태그(예: 태그1, 태그2)를 모두 가지고 있는 게시글이 하나 있다고 가정하면
  이 게시글의 벡터는 [1, 1]이 됩니다. (해당 태그 가지고 있으면 1, 없으면 0)
  이제 다른 게시글 하나를 임의로 가지고 오고, 이 게시글의 경우 태그1과 태그3을 가지고 있다고 하면,
  이 게시글의 벡터는 [1, 0]이 됩니다.

  코사인 유사도는 이 벡터 사이의 거리를 구한다는 개념입니다.
  배열의 인덱스0 값을 x축, 배열의 인덱스1 값을 y축이라고 생각했을 때,
  좌표평면 위에 게시글의 위치를 표시할 수 있습니다.
  이때 원점과 각 게시글의 벡터를 연결하는 선을 이은 후 그 사이의 각도를 코사인 유사도라고 이해하시면 됩니다.
  두 게시글의 벡터가 유사할수록 각도가 작아지고, 다를수록 각도가 커집니다.
  코사인 유사도는 이 각도에 반비례하는 값입니다.

   */

  @Transactional
  public Page<PostListDto.Response> recommendBasedOnInterest(
      Member member, PostStatus postStatus, PostOrder postOrder, Pageable pageable) {

    // 이전에 사용했던 테이블 삭제 (게시글은 계속 업데이트 되기 때문에 매번 새로 계산해주어야 함)
    recommendRepository.deleteAll();

    // 해당 유저의 관심 태그 목록 가져옴
    List<MemberTag> memberTags = memberTagRepository.findAllByMember_MemberId(member.getMemberId());
    List<Tag> interestTags = memberTags.stream().map(MemberTag::getTag).collect(Collectors.toList());

    // 관심 태그가 하나라도 포함되어 있는 게시글의 목록을 가져옴
    List<PostTag> possibleCandidates = postTagRepository.findDistinctByTagIn(interestTags);
    // 가져온 게시글들 중에서 진행 중 / 마감한 글만 걸러내기 -> 여기서부터는 전부 노출됨
    List<Post> possiblePosts = filterPostsByStatus(possibleCandidates, postStatus);

    // 위에서 걸러낸 게시글들 각각의 코사인 유사도를 구해서 RecommendRepository에 저장
    getCosineSimilarityOfPosts(possiblePosts, interestTags, interestTags.size());

    // 게시글을 우선 유사도가 높은 순으로 나열하고, 그 다음에 선택한 분류 기준 (최신순, 투표 마감 임박 순, 투표 참여 인원 많은 순, 채팅 참여 인원 많은 순) 적용
    // 관심 주제 태그가 최대 5개고 가중치가 같아서 코사인 유사도가 같은 항목이 많을 것으로 예상됨 -> 이 순서로 적용해도 괜찮을 것이라 판단
    List<PostListDto.Response> resultContents;

    if (PostOrder.IMMINENT_DEADLINE.equals(postOrder)) {
      List<Order> orders = new ArrayList<>();
      orders.add(new Order(Direction.DESC, "score"));
      orders.add(new Order(Direction.ASC, "post." + postOrder.getSortingCriteria()));
      resultContents = recommendRepository.findAll(Sort.by(orders))
          .stream().map(r -> PostListDto.Response.postToPostListResponse(r.getPost())).collect(Collectors.toList());
    } else {
      resultContents = recommendRepository.findAll(
          Sort.by(Direction.DESC, "score", "post." + postOrder.getSortingCriteria()))
          .stream().map(r -> PostListDto.Response.postToPostListResponse(r.getPost())).collect(Collectors.toList());
    }

    return PageableExecutionUtils.getPage(resultContents, pageable, () -> (long) resultContents.size());
  }

  private List<Post> filterPostsByStatus(List<PostTag> possibleCandidates, PostStatus postStatus) {
    // '진행 중' 탭일 경우 투표 마감 시간이 현재 이후인 글들만 뽑아옴
    if (PostStatus.ONGOING.equals(postStatus)) {
      return possibleCandidates.stream().map(PostTag::getPost).filter(
          post -> post.getEndAt().isAfter(LocalDateTime.now())
      ).distinct().collect(Collectors.toList());
    // '마감' 탭일 경우 투표 마감 시간이 현재 이전인 글들만 뽑아옴
    } else {
      return possibleCandidates.stream().map(PostTag::getPost).filter(
          post -> post.getEndAt().isBefore(LocalDateTime.now())
      ).distinct().collect(Collectors.toList());
    }
  }

  private void getCosineSimilarityOfPosts(List<Post> possiblePosts, List<Tag> interestTags, int n) {
    // 모든 관심 주제 태그를 가지고 있는 가상의 게시글의 벡터
    double[] targetVector = new double[n];
    Arrays.fill(targetVector, 1);
    // 게시글의 벡터 -> 똑같은 배열 계속 사용하려고 밖으로 뺌
    double[] tagVector = new double[n];

    // 게시글 목록의 각 게시글에 대하여
    for(Post post: possiblePosts) {
      // 해당 게시글의 태그 목록을 가져옴
      List<Tag> postTags =
          postTagRepository.findAllByPost_PostId(post.getPostId()).stream().map(PostTag::getTag).collect(Collectors.toList());
      for(int i = 0; i < n; i++) {
        // 각 관심 주제에 대해 해당 게시글의 태그 목록에 그 관심 주제가 포함되어 있으면 1, 없으면 0
        Tag interestTag = interestTags.get(i);
        tagVector[i] = postTags.contains(interestTag) ? 1 : 0;
      }

      // 벡터 값 산출이 끝나면 모든 관심 주제 태그를 달고 있는 글과의 코사인 유사도를 계산하여 저장함
      recommendRepository.save(Recommend.builder()
          .post(post)
          .score(calculateCosineSimilarity(targetVector, tagVector))
          .build());
    }
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

}
