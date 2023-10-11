package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.member.service.MemberService;
import com.example.solumonbackend.post.model.HasInterestTagsDto;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostDetailDto;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;
  private final MemberService memberService;

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<PostAddDto.Response> createPost(@AuthenticationPrincipal MemberDetail memberDetail,
                                                        @RequestPart("request") @Valid PostAddDto.Request request,
                                                        @RequestPart("images") List<MultipartFile> images) {
    return ResponseEntity.ok(postService.createPost(memberDetail.getMember(), request, images));
  }

  @GetMapping("/{postId}")
  public ResponseEntity<PostDetailDto.Response> getPostDetail(@AuthenticationPrincipal MemberDetail memberDetail,
                                                              @PathVariable long postId) {
    return ResponseEntity.ok(postService.getPostDetail(memberDetail.getMember(), postId));
  }

  @PutMapping(value = "/{postId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<PostUpdateDto.Response> updatePost(@AuthenticationPrincipal MemberDetail memberDetail,
                                                           @PathVariable long postId,
                                                           @RequestPart("request") @Valid PostUpdateDto.Request request,
                                                           @RequestPart("images") List<MultipartFile> images) {
    return ResponseEntity.ok(postService.updatePost(memberDetail.getMember(), postId, request, images));
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<String> deletePost(@AuthenticationPrincipal MemberDetail memberDetail,
                                           @PathVariable long postId) {
    postService.deletePost(memberDetail.getMember(), postId);
    return ResponseEntity.ok("게시글이 삭제되었습니다.");
  }

  @GetMapping("/post-list")
  public ResponseEntity<HasInterestTagsDto.Response> hasInterestTags(
      @AuthenticationPrincipal MemberDetail memberDetail) {

    return ResponseEntity.ok(HasInterestTagsDto.Response.builder()
        .hasInterestTags(memberService.hasInterestTags(memberDetail.getMember()))
        .build());
  }

}
