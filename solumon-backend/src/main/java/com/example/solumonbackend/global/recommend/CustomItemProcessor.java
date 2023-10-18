package com.example.solumonbackend.global.recommend;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.MemberTag;
import com.example.solumonbackend.member.repository.MemberTagRepository;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Recommend;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.RecommendRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class CustomItemProcessor implements ItemProcessor<Member, List<Recommend>> {

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

  // 넘겨받은 각각의 Member에 대해
  @Override
  public List<Recommend> process(Member member) throws Exception {
    // 만약 1시간 전에도 존재하던 멤버라면, 이 사람들의 현재 관심 포스트는 1) 그대로거나 2) 태그가 수정되었거나 3) 삭제됨
    // 기본적으로 1시간 전 이후에 작성된 포스트들은 추가해야 하고,
    // 1)의 포스트는 그대로 두되 2), 3)은 값을 새로 교체해주어야 함
    if (recommendRepository.existsByMemberId(member.getMemberId())) {
      // 해당 멤버의 관심 태그를 가져옴
      List<MemberTag> memberTags = memberTagRepository.findAllByMember_MemberId(
          member.getMemberId());
      List<Tag> interestTags = memberTags.stream().map(MemberTag::getTag)
          .collect(Collectors.toList());

      // 기존 포스트들 중 수정된(삭제된 포스트 포함) 포스트에 대한 recommend는 모두 삭제함
      // 0 벡터가 되었을(관련 태그가 모두 삭제되었을) 가능성을 대비해 update 대신 삭제 (코사인 유사도는 0 벡터로는 계산이 안 됨)
      List<Recommend> modifiedPostRecommends
          = recommendRepository.findAllByMemberIdAndPostModifiedAtIsAfter(member.getMemberId(),
          LocalDateTime.now().minusHours(1));
      recommendRepository.deleteAll(modifiedPostRecommends);

      // 1시간 전 이후부터 수정된 관심 태그가 있는 포스트들을 전부 가져옴 (삭제된 포스트 포함)
      List<Post> modifiedPosts
          = postTagRepository.findDistinctByTagInAndPostModifiedAtIsAfter(interestTags,
              LocalDateTime.now().minusHours(1))
          .stream().map(PostTag::getPost).distinct().collect(Collectors.toList());

      // 1시간 전 이후부터 작성된 관심 태그가 하나라도 있는 포스트들을 전부 가져옴
      List<Post> newPosts
          = postTagRepository.findDistinctByTagInAndPostCreatedAtIsAfter(interestTags,
              LocalDateTime.now().minusHours(1))
          .stream().map(PostTag::getPost).distinct().collect(Collectors.toList());

      // 각 포스트에 대하여 코사인 유사도 계산
      List<Recommend> fullList = new ArrayList<>();
      fullList.addAll(getCosineSimilarityOfPosts(modifiedPosts, member.getMemberId(), interestTags,
          interestTags.size()));
      fullList.addAll(getCosineSimilarityOfPosts(newPosts, member.getMemberId(), interestTags,
          interestTags.size()));

      return fullList;

      // 1시간 이내에 가입한 멤버라면
    } else {
      // 멤버의 관심 태그를 가져옴
      List<MemberTag> memberTags = memberTagRepository.findAllByMember_MemberId(
          member.getMemberId());
      List<Tag> interestTags = memberTags.stream().map(MemberTag::getTag)
          .collect(Collectors.toList());

      // 지금까지 존재하는 모든 포스트에 대하여 관심 태그가 하나라도 있는 포스트들을 전부 가져옴
      List<Post> possiblePosts = postTagRepository.findDistinctByTagIn(interestTags)
          .stream().map(PostTag::getPost).distinct().collect(Collectors.toList());

      // 각 포스트에 대하여 코사인 유사도 계산
      return getCosineSimilarityOfPosts(possiblePosts, member.getMemberId(), interestTags,
          interestTags.size());
    }
  }

  private List<Recommend> getCosineSimilarityOfPosts(
      List<Post> possiblePosts, Long memberId, List<Tag> interestTags, int interestTagsSize) {
    // 모든 관심 주제 태그를 가지고 있는 가상의 게시글의 벡터
    double[] targetVector = new double[interestTagsSize];
    Arrays.fill(targetVector, 1);
    // 게시글의 벡터 -> 똑같은 배열 계속 사용하려고 밖으로 뺌
    double[] tagVector = new double[interestTagsSize];

    List<Recommend> result = new ArrayList<>();

    // 게시글 목록의 각 게시글에 대하여
    for (Post post : possiblePosts) {
      // 해당 게시글의 태그 목록을 가져옴
      List<Tag> postTags =
          postTagRepository.findAllByPost_PostId(post.getPostId()).stream().map(PostTag::getTag)
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
}
