package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostState;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<Post> getMyParticipatePosts(Long memberId, PostParticipateType postParticipateType,
      PostState postState, PostOrder postOrder) {
    return null;
  }
}