package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.model.PostDto.ChoiceResultDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class VoteCustomRepository {

  @PersistenceContext
  private EntityManager em;

  public List<ChoiceResultDto> getChoiceResults(Long postId) {
    // choicePercent 도 함께 쿼리에서 계산하고 싶었으나 계속되는 오류로 제외하고 구함(queryDSL 공부 후 수정 예정)
    List<ChoiceResultDto> resultList = em.createQuery(
            "SELECT new com.example.solumonbackend.post.model.PostDto$ChoiceResultDto" +
                "(c.choiceNum, c.choiceText, COUNT(v.selectedNum), 0.0) "
                + "FROM Choice c "
                + "LEFT JOIN Vote v ON c.post.postId = v.post.postId AND c.choiceNum = v.selectedNum "
                + "WHERE c.post.postId = :postId "
                + "GROUP BY c.choiceNum, c.choiceText "
                + "having COUNT(*) > 0 "
                + "ORDER BY c.choiceNum", ChoiceResultDto.class)
        .setParameter("postId", postId)
        .getResultList();

    long countSum = resultList.stream().mapToLong(ChoiceResultDto::getChoiceCount).sum();

    return resultList.stream()
        .map(result -> ChoiceResultDto.builder()
            .choiceNum(result.getChoiceNum())
            .choiceText(result.getChoiceText())
            .choiceCount(result.getChoiceCount())
            .choicePercent(countSum == 0 ? 0.0 : result.getChoiceCount() * 100.0 / countSum)
            .build())
        .collect(Collectors.toList());
  }
}
