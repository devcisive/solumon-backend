package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.model.MyActivePostDto;
import com.example.solumonbackend.post.type.PostActiveType;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  @Query(value = "SELECT p.post_id, p.title, p.contents, p.created_at, p.member_member_id, "
      + "(SELECT COUNT(cm.post_post_id) FROM channel_member cm WHERE cm.post_post_id = p.post_id GROUP BY cm.post_post_id) AS channel_members, "
      + "(SELECT COUNT(vote.post_post_id) FROM vote WHERE vote.post_post_id = p.post_id GROUP BY vote.post_post_id) AS vote_members "
      + "FROM post p "
      + "JOIN vote v ON v.post_post_id = p.post_id "
      + "JOIN channel_member cm ON cm.post_post_id = p.post_id "
      + "WHERE "
      + "CASE "
      + "WHEN :postActiveType ='WRITE' THEN p.member_member_id = :memberId "
      + "WHEN :postActiveType ='VOTE' THEN v.member_member_id = :memberId "
      + "WHEN :postActiveType ='CHAT' THEN cm.member_member_id = :memberId "
      + "END "
      + "AND "
      + "CASE "
      + "WHEN :postState ='ONGOING' THEN  p.end_at IS NULL "
      + "WHEN :postState ='COMPLETED' THEN  p.end_at IS NOT NULL "
      + "ORDER BY "
      + "CASE "
      + "WHEN :postOrder = 'POST_ORDER' THEN p.created_at "
      + "WHEN :postOrder = 'MOST_VOTES' THEN vote_members "
      + "WHEN :postOrder = 'MOST_CHAT_PARTICIPANTS' THEN channel_members "
      + "END DESC", nativeQuery = true)
  List<MyActivePostDto> getMyActivePosts(
      @Param("memberId") Long memberId,
      @Param("postState") PostState postState,
      @Param("postActiveType") PostActiveType postActiveType,
      @Param("postOrder") PostOrder postOrder
  );

}
