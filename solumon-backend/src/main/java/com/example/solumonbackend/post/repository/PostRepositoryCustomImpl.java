package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.chat.entity.QChannelMember;
import com.example.solumonbackend.post.entity.QPost;
import com.example.solumonbackend.post.entity.QVote;
import com.example.solumonbackend.post.model.MyParticipatePostDto;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

/*
   * SELECT *
     FROM post
     WHERE
   -- 상태 조건 (stateCondition)
      [stateCondition]
   AND
   -- 참여 유형 조건 (participateTypeCondition)
   [participateTypeCondition]

    ORDER BY
   -- 정렬 조건 (orderSpecifier)
   [orderSpecifier]
*/


  @Override
  public Page<MyParticipatePostDto> getMyParticipatePostPages(Long memberId,
      PostParticipateType postParticipateType, PostStatus postStatus, PostOrder postOrder,
      Pageable pageable) {

    QPost qPost = QPost.post;

    // 조건1 (참여타입)
    BooleanExpression participateTypeCondition
        = createParticipateTypeCondition(memberId, postParticipateType, qPost);
    // 조건2 (상태)
    BooleanExpression stateCondition = createStateCondition(postStatus, qPost);

    // 정렬기준
    OrderSpecifier<?> orderSpecifier = createOrderSpecifier(postOrder, qPost);

    // 가져올 데이터
    List<MyParticipatePostDto> resultContents
        = jpaQueryFactory.select(Projections.bean(MyParticipatePostDto.class,
            qPost.postId,
            qPost.member.nickname,
            qPost.title,
            qPost.contents,
            qPost.chatCount,
            qPost.voteCount,
            qPost.thumbnailUrl,
            qPost.createdAt
        ))
        .from(qPost)
        .where(stateCondition)
        .where(participateTypeCondition)
        .orderBy(orderSpecifier)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 조건에 맞는 데이터 총 개수 구하는 count 쿼리 (실행 전의 상태)
    JPAQuery<Long> totalCount = jpaQueryFactory.select(qPost.count()).from(qPost)
        .where(stateCondition)
        .where(participateTypeCondition);

    return PageableExecutionUtils.getPage(resultContents, pageable, totalCount::fetchOne);
     /*
      1. 첫번째 페이지이면서 콘텐츠사이즈가 한 페이지의 사이즈보다 작을때
      2. 마지막페이지일때
      이 외의 경우에만  count 쿼리가 실행됨
    * */

  }


  // 게시물 상태에 따른 조건 (ONGOING: 진행중 , COMPLETED: 마감 )
  private BooleanExpression createStateCondition(PostStatus status, QPost qPost) {

    if (qPost == null) {
      throw new NullPointerException("Qpost is null");
    }

    if (status == null){
      throw new NullPointerException("PostStatus is null");
    }

    if (status == PostStatus.ONGOING) {
      return qPost.endAt.after(LocalDateTime.now());
    }

    if (status == PostStatus.COMPLETED) {
      return qPost.endAt.before(LocalDateTime.now());
    }

    throw new IllegalArgumentException("PostStatus의 값이 잘못되었습니다.");
  }

  // 내가 게시글에 참여한 타입에 따른 조건 (CHAT: 채팅 , VOTE: 투표 , WRITE: 작성)
  private BooleanExpression createParticipateTypeCondition(
      Long memberId, PostParticipateType participateType, QPost qPost) {

    if (qPost == null) {
      throw new NullPointerException("Qpost is null");
    }

    if (memberId == null){
      throw new NullPointerException("memberId is null");
    }

    if (participateType == null) {
      throw new NullPointerException("PostParticipateType is null");
    }

    // 채팅
    if (participateType == PostParticipateType.CHAT) {
      QChannelMember channelMember = QChannelMember.channelMember;
      return qPost.postId.in(
          JPAExpressions
              .select(channelMember.post.postId)
              .from(channelMember)
              .where(channelMember.member.memberId.eq(memberId))
      );
    }

    // 투표
    if (participateType == PostParticipateType.VOTE) {
      QVote qVote = QVote.vote;
      return qPost.postId.in(
          JPAExpressions
              .select(qVote.post.postId)
              .from(qVote)
              .where(qVote.member.memberId.eq(memberId))
      );
    }

    // 작성
    if (participateType == PostParticipateType.WRITE) {
      return qPost.member.memberId.eq(memberId);
    }

    throw new IllegalArgumentException("PostParticipateType의 값이 잘못되었습니다.");
  }


  // Order By 값 구하기
  private OrderSpecifier<?> createOrderSpecifier(PostOrder order, QPost qPost) {

    if (qPost == null) {
      throw new NullPointerException("Qpost is null");
    }

    if (order == null) {
      throw new NullPointerException("PostOrder is null");
    }

    // 최신순)  post.createdAt.desc()
    if (order == PostOrder.LATEST) {
      return qPost.createdAt.desc(); // OrderSpecifier<LocalDateTime>
    }

    // 투표참여인원)  post.voteCount.desc()
    if (order == PostOrder.MOST_VOTES) {
      return qPost.voteCount.desc(); // OrderSpecifier<Integer>
    }

    // 채팅참여인원)  post.chatCount.desc()
    if (order == PostOrder.MOST_CHAT_PARTICIPANTS) {
      return qPost.chatCount.desc();  // OrderSpecifier<Integer>
    }

    // 마감 임박 순)  post.endAt.desc()
    if (order == PostOrder.IMMINENT_CLOSE) {
      return qPost.endAt.desc();  // OrderSpecifier<LocalDateTime>
    }

    throw new IllegalArgumentException("PostOrder의 값이 잘못되었습니다.");

  }


  @Override
  public Page<PostListDto.Response> getGeneralPostList(PostStatus postStatus, PostOrder postOrder,
      Pageable pageable) {
    QPost qPost = QPost.post;

    // 조건1 (상태)
    BooleanExpression stateCondition = createStateCondition(postStatus, qPost);

    // 가져올 데이터
    List<PostListDto.Response> resultContents
        = jpaQueryFactory.select(Projections.constructor(PostListDto.Response.class,
            qPost.postId,
            qPost.title,
            qPost.member.nickname.as("writer"),
            qPost.contents,
            qPost.thumbnailUrl.as("imageUrl"),
            qPost.voteCount,
            qPost.chatCount,
            qPost.createdAt
        ))
        .from(qPost)
        .where(stateCondition)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 조건에 맞는 데이터 총 개수 구하는 count 쿼리 (실행 전의 상태)
    JPAQuery<Long> totalCount = jpaQueryFactory.select(qPost.count()).from(qPost)
        .where(stateCondition);

    return PageableExecutionUtils.getPage(resultContents, pageable, totalCount::fetchOne);
  }

}