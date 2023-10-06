package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.service.PostService;
import com.example.solumonbackend.post.service.RecommendationService;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import com.example.solumonbackend.post.type.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;
  private final RecommendationService recommendationService;

  @PostMapping
  public ResponseEntity<?> createPost(@AuthenticationPrincipal MemberDetail memberDetail,
                                      @RequestBody PostAddDto.Request dto) {
    return ResponseEntity.ok(postService.createPost(memberDetail.getMember(), dto));
  }

  @GetMapping("/{postId}")
  public ResponseEntity<?> getPostDetail(@AuthenticationPrincipal MemberDetail memberDetail,
                                         @PathVariable long postId) {
    return ResponseEntity.ok(postService.getPostDetail(memberDetail.getMember(), postId));
  }

  @PutMapping("/{postId}")
  public ResponseEntity<?> updatePost(@AuthenticationPrincipal MemberDetail memberDetail,
                                      @PathVariable long postId,
                                      @RequestBody PostUpdateDto.Request request) {
    return ResponseEntity.ok(postService.updatePost(memberDetail.getMember(), postId, request));
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<?> deletePost(@AuthenticationPrincipal MemberDetail memberDetail,
                                      @PathVariable long postId) {
    postService.deletePost(memberDetail.getMember(), postId);
    return ResponseEntity.ok("게시글이 삭제되었습니다.");
  }

  @GetMapping
  public ResponseEntity<Page<PostListDto.Response>> getPosts(@AuthenticationPrincipal MemberDetail memberDetail,
      @RequestParam PostType postType, @RequestParam PostStatus postStatus, @RequestParam PostOrder postOrder,
      @RequestParam(defaultValue = "1") Integer pageNum) {
    if (PostType.INTEREST.equals(postType)) {
      return ResponseEntity.ok(
          recommendationService.recommendBasedOnInterest(
              memberDetail.getMember(), postStatus, postOrder, PageRequestCustom.of(pageNum)));
    } else {
      return ResponseEntity.ok(Page.empty());
    }
  }
}
