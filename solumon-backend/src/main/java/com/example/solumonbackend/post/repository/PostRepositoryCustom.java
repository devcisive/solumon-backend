package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.model.MyParticipatePostDto;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostParticipateType;
import com.example.solumonbackend.post.type.PostState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

  Page<MyParticipatePostDto> getMyParticipatePostPages(Long memberId,
      PostParticipateType postParticipateType, PostState postState, PostOrder postOrder,
      Pageable pageable);

}
