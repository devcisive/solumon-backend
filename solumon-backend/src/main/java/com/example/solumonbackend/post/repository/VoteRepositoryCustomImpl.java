package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.QChoice;
import com.example.solumonbackend.post.entity.QVote;
import com.example.solumonbackend.post.model.PostDto.ChoiceResultDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class VoteRepositoryCustomImpl implements VoteRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  public List<ChoiceResultDto> getChoiceResults(Long postId) {
    QChoice qChoice = QChoice.choice;
    QVote qVote = QVote.vote;

    // 전체 투표 수를 구하는 서브쿼리
    Expression<Long> totalVotes = JPAExpressions.select(qVote.count())
        .from(qVote)
        .where(qVote.post.postId.eq(postId));

    // 투표 수(count)와 전체 투표 수에 대한 퍼센트 값을 계산하여 ChoiceResultDto를 생성
    List<ChoiceResultDto> dtoList = jpaQueryFactory
        .select(Projections.constructor(ChoiceResultDto.class,
            qChoice.choiceNum,
            qChoice.choiceText,
            qVote.count().as("choiceCount"),
            qVote.count().divide(totalVotes).multiply(100).as("choicePercent")))
        .from(qChoice)
        .leftJoin(qVote).on(qChoice.post.postId.eq(qVote.post.postId),
            qChoice.choiceNum.eq(qVote.selectedNum))
        .where(qChoice.post.postId.eq(postId))
        .groupBy(qChoice.choiceNum, qChoice.choiceText)
        .fetch();

    return dtoList.stream()
        .map(dto -> dto.getChoicePercent() == null ? dto.setChoicePercent(0L) : dto)
        .collect(Collectors.toList());
  }

}
