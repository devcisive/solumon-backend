package com.example.solumonbackend.global.elasticsearch;

import com.example.solumonbackend.post.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Document(indexName = "posts")
@Mapping(mappingPath = "elasticsearch/search-mapping.json")
@Setting(settingPath = "elasticsearch/search-settings.json")
public class PostDocument {
  @Id
  private Long postId;
  private String title;
  private String contents;
  private String writer;
  private String imageUrl;
  private int voteCount;
  private int chatCount;
  private LocalDateTime endAt;
  private LocalDateTime createdAt;
  private String tags;

  @Builder
  public PostDocument(Post post, List<String> tags) {
    this.postId = post.getPostId();
    this.title = post.getTitle();
    this.contents = post.getContents();
    this.writer = post.getMember().getNickname();
    this.imageUrl = post.getThumbnailUrl();
    this.voteCount = post.getVoteCount();
    this.chatCount = post.getChatCount();
    this.endAt = post.getEndAt();
    this.createdAt = post.getCreatedAt();
    this.tags = tags.toString();
  }

  public void updatePostDocument(Post post, List<String> tags) {
    this.title = post.getTitle();
    this.contents = post.getContents();
    this.imageUrl = post.getThumbnailUrl();
    this.voteCount = post.getVoteCount();
    this.chatCount = post.getChatCount();
    this.endAt = post.getEndAt();
    this.createdAt = post.getCreatedAt();
    this.tags = tags.toString();
  }

}
