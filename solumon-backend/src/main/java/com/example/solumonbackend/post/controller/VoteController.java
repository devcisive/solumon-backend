package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.model.VoteAddDto;
import com.example.solumonbackend.post.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/vote")
@RequiredArgsConstructor
public class VoteController {

  private final VoteService voteService;

  @PostMapping
  public ResponseEntity<?> createVote(@AuthenticationPrincipal Member member,
                                      @PathVariable long postId,
                                      @RequestBody VoteAddDto.Request dto) {
    return ResponseEntity.ok(voteService.createVote(member, postId, dto));
  }

  @DeleteMapping
  public ResponseEntity<?> deleteVote(@AuthenticationPrincipal Member member,
                                      @PathVariable long postId) {
    voteService.deleteVote(member, postId);
    return ResponseEntity.ok("게시글이 삭제되었습니다.");
  }

}
