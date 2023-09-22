package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

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

}
