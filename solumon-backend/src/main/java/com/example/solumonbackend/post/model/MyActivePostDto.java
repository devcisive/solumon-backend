package com.example.solumonbackend.post.model;

import java.time.LocalDateTime;

public class MyActivePostDto {

  private Long postId;

  private String writerNickname;

  private String title;

  private String contents;

  private LocalDateTime createdAt;

  private Long memberId;

  private LocalDateTime endAt;

//  private List<String> imageUrls;

  private Long voteMemberCount;

  private Long chatMemberCount;


  public MyActivePostDto(Long postId, String title, String contents, LocalDateTime createdAt,
      Long memberId, Long chatMemberCount, Long voteMemberCount) {
    this.postId = postId;
    this.title = title;
    this.contents = contents;
    this.createdAt = createdAt;
    this.memberId = memberId;
    this.chatMemberCount = chatMemberCount;
    this.voteMemberCount = voteMemberCount;
  }

//  public static List<MyActivePostDto> getMyActivePostDtos (List<Post> myPosts){
//    List<MyActivePostDto> myActivePostDtos = new ArrayList<>();
//
//    for(Post post : myPosts){
//      myActivePostDtos.add(MyActivePostDto.fromPost(post));
//    }
//
//    return myActivePostDtos;
//  }
//
//  public static MyActivePostDto fromPost(Post post){
//    return MyActivePostDto.builder()
//                    .postId(post.getPostId())
//                    .title(post.getTitle())
//                    .contents(post.getContents())
////                    .imageUrls(post.getImageUrls())
//                    .build();
//  }
}
