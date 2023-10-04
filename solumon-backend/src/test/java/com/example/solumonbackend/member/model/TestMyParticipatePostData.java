package com.example.solumonbackend.member.model;

import com.example.solumonbackend.chat.entity.ChannelMember;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.Vote;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.util.Pair;


public class TestMyParticipatePostData {

  public static List<Post> myWritePosts(Member fakeMember) {
    List<Post> myWritePosts = new ArrayList<>();

    myWritePosts.add(
        Post.builder().member(fakeMember).title("myWritePost1").contents("최신5,마감,투표5,채팅1")
            .createdAt(LocalDateTime.now().minusDays(5)).endAt(LocalDateTime.now().minusDays(1))
            .voteCount(10).chatCount(50).build());

    myWritePosts.add(
        Post.builder().member(fakeMember).title("myWritePost2").contents("최신4,마감,투표4,채팅2")
            .createdAt(LocalDateTime.now().minusDays(4)).endAt(LocalDateTime.now().minusDays(1))
            .voteCount(20).chatCount(40).build());

    myWritePosts.add(
        Post.builder().member(fakeMember).title("myWritePost3").contents("최신3,마감,투표3,채팅3")
            .createdAt(LocalDateTime.now().minusDays(3)).endAt(LocalDateTime.now().minusDays(1))
            .voteCount(30).chatCount(30).build());

    myWritePosts.add(
        Post.builder().member(fakeMember).title("myWritePost4").contents("최신2,진행,투표2,채팅4")
            .createdAt(LocalDateTime.now().minusDays(1)).endAt(LocalDateTime.now().plusDays(1))
            .voteCount(40).chatCount(20).build());

    myWritePosts.add(
        Post.builder().member(fakeMember).title("myWritePost5").contents("최신1,진행,투표1,채팅5")
            .createdAt(LocalDateTime.now().minusDays(1)).endAt(LocalDateTime.now().plusDays(1))
            .voteCount(50).chatCount(10).build());

    return myWritePosts;
  }

  public static Pair<List<Post>, List<Vote>> myVotePosts(Member fakeMember, Member otherMember) {
    List<Post> myVotePosts = new ArrayList<>();
    List<Vote> myVotes;

    myVotePosts.add(
        Post.builder().member(otherMember).title("myVotePost1").contents("최신5,마감,투표5,채팅1")
            .createdAt(LocalDateTime.now().minusDays(5)).endAt(LocalDateTime.now().plusDays(1))
            .voteCount(10).chatCount(50).build());

    myVotePosts.add(
        Post.builder().member(otherMember).title("myVotePost2").contents("최신4,마감,투표4,채팅2")
            .createdAt(LocalDateTime.now().minusDays(4)).endAt(LocalDateTime.now().plusDays(1))
            .voteCount(20).chatCount(40).build());

    myVotePosts.add(
        Post.builder().member(otherMember).title("myVotePost3").contents("최신3,마감,투표3,채팅3")
            .createdAt(LocalDateTime.now().minusDays(3)).endAt(LocalDateTime.now().plusDays(1))
            .voteCount(30).chatCount(30).build());

    myVotePosts.add(
        Post.builder().member(otherMember).title("myVotePost4").contents("최신2,진행,투표2,채팅4")
            .createdAt(LocalDateTime.now().minusDays(1)).endAt(LocalDateTime.now().minusDays(1))
            .voteCount(40).chatCount(20).build());

    myVotePosts.add(
        Post.builder().member(otherMember).title("myVotePost5").contents("최신1,진행,투표1,채팅5")
            .createdAt(LocalDateTime.now().minusDays(1)).endAt(LocalDateTime.now().minusDays(1))
            .voteCount(50).chatCount(10).build());

    myVotes = myVotePosts.stream()
        .map(post -> Vote.builder().post(post).member(fakeMember).build())
        .collect(Collectors.toList());

    return Pair.of(myVotePosts, myVotes);
  }

  public static Pair<List<Post>, List<ChannelMember>> myChatPosts(Member fakeMember,
      Member otherMember) {
    List<Post> myChatPosts = new ArrayList<>();
    List<ChannelMember> myChats;

    myChatPosts.add(
        Post.builder().member(otherMember).title("myChatPost1").contents("최신5,마감,투표5,채팅1")
            .createdAt(LocalDateTime.now().minusDays(5)).endAt(LocalDateTime.now().plusDays(1))
            .voteCount(10).chatCount(50).build());

    myChatPosts.add(
        Post.builder().member(otherMember).title("myChatPost2").contents("최신4,마감,투표4,채팅2")
            .createdAt(LocalDateTime.now().minusDays(4)).endAt(LocalDateTime.now().plusDays(1))
            .voteCount(20).chatCount(40).build());

    myChatPosts.add(
        Post.builder().member(otherMember).title("myChatPost3").contents("최신3,마감,투표3,채팅3")
            .createdAt(LocalDateTime.now().minusDays(3)).endAt(LocalDateTime.now().plusDays(1))
            .voteCount(30).chatCount(30).build());

    myChatPosts.add(
        Post.builder().member(otherMember).title("myChatPost4").contents("최신2,진행,투표2,채팅4")
            .createdAt(LocalDateTime.now().minusDays(1)).endAt(LocalDateTime.now().minusDays(1))
            .voteCount(40).chatCount(20).build());

    myChatPosts.add(
        Post.builder().member(otherMember).title("myChatPost5").contents("최신1,진행,투표1,채팅5")
            .createdAt(LocalDateTime.now().minusDays(1)).endAt(LocalDateTime.now().minusDays(1))
            .voteCount(50).chatCount(10).build());

    myChats = myChatPosts.stream()
        .map(post -> ChannelMember.builder().post(post).member(fakeMember).build())
        .collect(Collectors.toList());

    return Pair.of(myChatPosts, myChats);
  }

}
