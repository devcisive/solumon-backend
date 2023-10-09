package com.example.solumonbackend.post.type;

public enum PostOrder {
  LATEST("최신순"),
  MOST_VOTES("투표참여순"),
  MOST_CHAT_PARTICIPANTS("채팅참여인원순");

  private final String orderType;

  PostOrder(String orderType){
    this.orderType = orderType;
  }

  public String getOrderTarget(){
    return this.orderType;
  };
}
