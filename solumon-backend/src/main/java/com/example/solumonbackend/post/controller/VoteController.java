package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.member.model.MemberDetail;
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
  public ResponseEntity<VoteAddDto.Response> createVote(@AuthenticationPrincipal MemberDetail memberDetail,
                                                        @PathVariable long postId,
                                                        @RequestBody VoteAddDto.Request request) {
    return ResponseEntity.ok(voteService.createVote(memberDetail.getMember(), postId, request.getSelectedNum()));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteVote(@AuthenticationPrincipal MemberDetail memberDetail,
                                         @PathVariable long postId) {
    voteService.deleteVote(memberDetail.getMember(), postId);
    return ResponseEntity.ok().build();
  }

}
