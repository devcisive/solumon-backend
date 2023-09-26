package com.example.solumonbackend.post.type;

public enum PostOrder {
  POST_ORDER("created_at"),
  MOST_VOTES("vote_count"),
  MOST_CHAT_PARTICIPANTS("chat_count");

  private final String orderType;

  PostOrder(String orderType){
    this.orderType = orderType;
  }

  public String getOrderTarget(){
    return this.orderType;
  };
}
