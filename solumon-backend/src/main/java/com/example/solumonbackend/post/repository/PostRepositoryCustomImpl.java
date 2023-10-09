package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.chat.entity.QChannelMember;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
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

    QPost qpost = QPost.post;

    // 조건1 (참여타입)
    BooleanExpression participateTypeCondition
        = createParticipateTypeCondition(memberId, postParticipateType, qpost);
    // 조건2 (상태)
    BooleanExpression stateCondition = createStateCondition(postStatus, qpost);

    // 정렬기준
    OrderSpecifier<?> orderSpecifier = createOrderSpecifier(postOrder, qpost);

    // 가져올 데이터
    List<MyParticipatePostDto> resultContents
        = jpaQueryFactory.select(Projections.bean(MyParticipatePostDto.class,
            qpost.postId,
            qpost.member.nickname.as("writerNickname"), // MyParticipatePostDto 의 필드이름과 일치하지않다면 as()사용
            qpost.title,
            qpost.contents,
            qpost.chatCount,
            qpost.voteCount,
            qpost.thumbnailUrl,
            qpost.createdAt
        ))
        .from(qpost)
        .where(stateCondition)
        .where(participateTypeCondition)
        .orderBy(orderSpecifier)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 조건에 맞는 데이터 총 개수 구하는 count 쿼리 (실행 전의 상태)
    JPAQuery<Long> totalCount = jpaQueryFactory.select(qpost.count()).from(qpost)
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
  private BooleanExpression createStateCondition(PostStatus state, QPost qpost) {
    // 예외처리에 대해서는 확정X (커스텀 or 표준 &처리방법)
    if (qpost == null || state == null) {
      throw new PostException(ErrorCode.NullPointerException);
    }

    if (state == PostStatus.ONGOING) {
      return qpost.endAt.after(LocalDateTime.now());
    }

    if (state == PostStatus.COMPLETED) {
      return qpost.endAt.before(LocalDateTime.now());
    }

    throw new PostException(ErrorCode.IllegalArgumentException);
  }


  // 내가 게시글에 참여한 타입에 따른 조건 (CHAT: 채팅 , VOTE: 투표 , WRITE: 작성)
  private BooleanExpression createParticipateTypeCondition(
      Long memberId, PostParticipateType participateType, QPost qPost) {

    // 예외처리에 대해서는 확정X (커스텀 or 표준 &처리방법)
    if (memberId == null || participateType == null) {
      throw new PostException(ErrorCode.NullPointerException);
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
      QVote vote = QVote.vote;
      return qPost.postId.in(
          JPAExpressions
              .select(vote.post.postId)
              .from(vote)
              .where(vote.member.memberId.eq(memberId))
      );
    }

    // 작성
    if (participateType == PostParticipateType.WRITE) {
      return qPost.member.memberId.eq(memberId);
    }

    throw new PostException(ErrorCode.IllegalArgumentException);
  }


  // Order By 값 구하기
  private OrderSpecifier<?> createOrderSpecifier(PostOrder order, QPost qpost) {
    // 예외처리에 대해서는 확정X (커스텀 or 표준 &처리방법)
    if (qpost == null || order == null) {
      throw new PostException(ErrorCode.NullPointerException);
    }

    // 최신순)  post.createdAt.desc()
    if (order == PostOrder.LATEST) {
      return qpost.createdAt.desc(); // OrderSpecifier<LocalDateTime>
    }

    // 투표참여인원)  post.voteCount.desc()
    if (order == PostOrder.MOST_VOTES) {
      return qpost.voteCount.desc(); // OrderSpecifier<Integer>
    }

    // 채팅참여인원)  post.chatCount.desc()
    if (order == PostOrder.MOST_CHAT_PARTICIPANTS) {
      return qpost.chatCount.desc();  // OrderSpecifier<Integer>
    }

    // 마감 임박 순)  post.endAt.desc()
    if (order == PostOrder.IMMINENT_CLOSE) {
      return qpost.endAt.desc();  // OrderSpecifier<LocalDateTime>
    }

    throw new PostException(ErrorCode.IllegalArgumentException);

  }

  @Override
  public Page<PostListDto.Response> getGeneralPostList(PostStatus postStatus, PostOrder postOrder,
      Pageable pageable) {
    QPost qpost = QPost.post;

    // 조건1 (상태)
    BooleanExpression stateCondition = createStateCondition(postStatus, qpost);

    // 가져올 데이터
    List<PostListDto.Response> resultContents
        = jpaQueryFactory.select(Projections.constructor(PostListDto.Response.class,
            qpost.postId,
            qpost.title,
            qpost.member.nickname.as("writer"),
            qpost.contents,
            qpost.thumbnailUrl.as("imageUrl"),
            qpost.voteCount,
            qpost.chatCount,
            qpost.createdAt
        ))
        .from(qpost)
        .where(stateCondition)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 조건에 맞는 데이터 총 개수 구하는 count 쿼리 (실행 전의 상태)
    JPAQuery<Long> totalCount = jpaQueryFactory.select(qpost.count()).from(qpost)
        .where(stateCondition);

    return PageableExecutionUtils.getPage(resultContents, pageable, totalCount::fetchOne);
  }

}