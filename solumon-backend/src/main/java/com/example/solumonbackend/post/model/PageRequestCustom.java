package com.example.solumonbackend.post.model;

import org.springframework.data.domain.PageRequest;


public final class PageRequestCustom {
/*
기본처리방법인 Pageable 로 받았을때 (Pageable -> PageRequest)
Pageable 의 size값의 limit가 없는 문제, page가 0부터 시작하는 점 등에 따른
프론트에서 값을 잘못 내려받을 수도 있는 가능성 등의 문제를 고려하여 커스텀클래스를 만들었음
* */

  private int page; //선택한 페이지
  private static final int size = 10;  // 페이지당 가져올 데이터 수


  public void setPage(int page){
    this.page = page <= 0 ? 1 : page;
  }


  public PageRequest of() {
    return PageRequest.of(page -1, size); // 페이지가 1부터 시작하는 것을 조정 (1페이지를 보고싶을땐 실제로 0페이지에 해당하는 데이터 넘겨야함)
  }
}

