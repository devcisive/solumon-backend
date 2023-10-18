package com.example.solumonbackend.post.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostOrder {
  LATEST( "createdAt"),
  MOST_VOTES("voteCount"),
  IMMINENT_CLOSE("endAt"),
  MOST_CHAT_PARTICIPANTS("chatCount");

  private final String sortCriteria;
}
