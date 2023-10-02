package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.model.PostDto.ChoiceResultDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class VoteCustomRepository {

  @PersistenceContext
  private EntityManager em;

  public List<ChoiceResultDto> getChoiceResults(Post post) {
    return em.createQuery(
            "SELECT NEW com.example.solumonbackend.post.model.PostDto.ChoiceResultDto(c.choiceNum, c.choiceText, " +
                "COUNT(*), (COUNT(*) * 100.0 / SUM(COUNT(*)) OVER ())) "
                + "FROM Choice c "
                + "JOIN Vote v ON c.post = v.post AND c.choiceNum = v.selectedNum "
                + "WHERE c.post = :post "
                + "GROUP BY c.choiceNum, c.choiceText "
                + "ORDER BY c.choiceNum", ChoiceResultDto.class)
        .setParameter("post", post)
        .getResultList();
  }
}
