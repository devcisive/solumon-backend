package com.example.solumonbackend.member.model;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.model.MyActivePostDto;
import java.util.List;
import lombok.Builder;

public class MemberLogDto {

  @Builder
  public static class Info{

    private Long member_id;
    private String nickname;
    private String email;
    private List<String> interests;

    public static MemberLogDto.Info of(Member member, List<String> interests){
      return MemberLogDto.Info.builder()
          .member_id(member.getMemberId())
          .nickname(member.getNickname())
          .interests(interests)
          .build();
    }
  }

//  @Builder
//  public static class Activity{
//
//    private Long member_id;
//    private List<MyActivePostDto> myPosts;
//    private int voteCount;
//    private int chatCount;
//
//    public static MemberLogDto.Activity of(Member member, List<Post> myPostDtos){
//     return MemberLogDto.Activity.builder()
//                                 .member_id(member.getMemberId())
//                                 .myPosts(MyActivePostDto.getMyPostDtos(myPostDtos))
//                                 .voteCount(1)
//                                 .chatCount(1)
//                                 .build();
//
//    }
//
//  }

}
