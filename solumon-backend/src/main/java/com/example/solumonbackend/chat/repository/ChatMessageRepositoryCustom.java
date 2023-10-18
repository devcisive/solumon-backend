package com.example.solumonbackend.chat.repository;

import com.example.solumonbackend.chat.model.ChatMessageDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatMessageRepositoryCustom {

  Slice<ChatMessageDto.Response> getLastChatMessagesScroll(Long postId, Long lastChatMessageId,
      Pageable pageable);
}
