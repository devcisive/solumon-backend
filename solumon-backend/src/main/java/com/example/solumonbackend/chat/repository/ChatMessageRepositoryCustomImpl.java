package com.example.solumonbackend.chat.repository;

import com.example.solumonbackend.chat.entity.QChatMessage;
import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ChatMessageRepositoryCustomImpl implements ChatMessageRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;
  private final QChatMessage qChatMessage = QChatMessage.chatMessage;

  @Override
  public Slice<ChatMessageDto.Response> getLastChatMessagesScroll(Long postId, Long lastChatMessageId,
      Pageable pageable) {

    // lt: less than
    // gt: greater than
    // goe: greater than or equal to

    List<ChatMessageDto.Response> lastChatMessages
        = jpaQueryFactory.select(Projections.bean(ChatMessageDto.Response.class,
            qChatMessage.postId,
            qChatMessage.nickname,
            qChatMessage.contents,
            qChatMessage.createdAt
        ))
        .from(qChatMessage)
        .where(qChatMessage.isSent.eq(true))
        .where(qChatMessage.postId.eq(postId))

        .where(ltChatMessageId(lastChatMessageId)) // 마지막으로 조회된 데이터번호보다 작은 데이터
        .orderBy(qChatMessage.messageId.desc())
        .limit(pageable.getPageSize() + 1) // 다음에 가져올 데이터의 존재여부를 확인하기 위해 1개 더 가져온다.
        .fetch();


    // 가져온 데이터의 수가 사이즈보다 크다면 다음에 가져올 데이터가 있다는 뜻
    boolean hasNext = false;
    if (lastChatMessages.size() > pageable.getPageSize()) {
      lastChatMessages.remove(pageable.getPageSize()); // 다음 데이터 존재여부 확인용으로 가져온 데이터는 제외해준다.
      hasNext = true;
    }

    return new SliceImpl<>(lastChatMessages, pageable, hasNext); // 다음 데이터의 존재여부를 같이 넣어서 반환
  }



  // 처음 시작할 땐 마지막으로 노출된 데이터가 없으니 조건없이 내림차순 한 것에서 10개만 가져오기
  private BooleanExpression ltChatMessageId(Long lastChatMessageId) {
    return lastChatMessageId != null ? qChatMessage.messageId.lt(lastChatMessageId) : null;
  }


}
