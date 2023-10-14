package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.model.MyParticipatePostDto;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

  Page<MyParticipatePostDto> getMyParticipatePostPages(Long memberId,
      PostParticipateType postParticipateType, PostStatus postStatus, PostOrder postOrder,
      Pageable pageable);

  Page<PostListDto.Response> getGeneralPostList(PostStatus postStatus, PostOrder postOrder,
      Pageable pageable);
}
