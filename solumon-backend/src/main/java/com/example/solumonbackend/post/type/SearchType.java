package com.example.solumonbackend.post.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchType {
  CONTENT("content"),
  TAG("tags");

  private final String searchType;
}
