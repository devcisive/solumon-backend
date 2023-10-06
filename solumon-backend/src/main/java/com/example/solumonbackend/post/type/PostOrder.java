package com.example.solumonbackend.post.type;

import lombok.Getter;

@Getter
public enum PostOrder {
  LATEST("최신순", "createdAt"),
  IMMINENT_DEADLINE("마감임박순", "endAt"),
  MOST_VOTES("투표참여순", "voteCount"),
  MOST_CHAT_PARTICIPANTS("채팅참여인원순", "chatCount");

  private final String orderType;
  private final String sortingCriteria;

  PostOrder(String orderType, String sortingCriteria){
    this.orderType = orderType;
    this.sortingCriteria = sortingCriteria;
  }
}
