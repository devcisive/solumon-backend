package com.example.solumonbackend.post.type;

public enum PostOrder {
  LATEST("최신순", "createdAt"),
  MOST_VOTES("투표참여순", "voteCount"),
  IMMINENT_CLOSE("마감임박순", "endAt"),
  MOST_CHAT_PARTICIPANTS("채팅참여인원순", "chatCount");

  private final String orderType;

  PostOrder(String orderType){
    this.orderType = orderType;
  }

  public String getOrderTarget(){
    return this.orderType;
  };
}
