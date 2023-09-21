package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.member.entity.Member;
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
  public ResponseEntity<?> createPost(@AuthenticationPrincipal Member member,
                                      @RequestBody PostAddDto.Request dto) {
    return ResponseEntity.ok(postService.createPost(member, dto));
  }

  @GetMapping("/{postId}")
  public ResponseEntity<?> getPostDetail(@AuthenticationPrincipal Member member,
                                         @PathVariable long postId) {
    return ResponseEntity.ok(postService.getPostDetail(member, postId));
  }

  @PutMapping("/{postId}")
  public ResponseEntity<?> updatePost(@AuthenticationPrincipal Member member,
                                      @PathVariable long postId,
                                      @RequestBody PostUpdateDto.Request request) {
    return ResponseEntity.ok(postService.updatePost(member, postId, request));
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<?> deletePost(@AuthenticationPrincipal Member member,
                                      @PathVariable long postId) {
    postService.deletePost(member, postId);
    return ResponseEntity.ok("게시글이 삭제되었습니다.");
  }

}
