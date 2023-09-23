package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostState;
import java.util.List;

public interface PostRepositoryCustom {

  List<Post> getMyParticipatePosts(Long memberId,
      PostParticipateType postParticipateType, PostState postState, PostOrder postOrder);
}
