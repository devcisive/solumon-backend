package com.example.solumonbackend.post.controller;
import com.example.solumonbackend.global.elasticsearch.PostSearchService;
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
  private final PostSearchService postSearchService;

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

  // 일반 조회
  @GetMapping
  public ResponseEntity<Page<PostListDto.Response>> getPostList(@AuthenticationPrincipal MemberDetail memberDetail,
      @RequestParam PostType postType,
      @RequestParam PostStatus postStatus,
      @RequestParam PostOrder postOrder,
      @RequestParam(defaultValue = "1") Integer pageNum) {

    if (postType == PostType.GENERAL) {
      return ResponseEntity.ok(postService.getGeneralPostList(postStatus, postOrder, pageNum));
    } else {
      // 관심목록 조회
      return null;
    }
  }

  @GetMapping("/search")
  public ResponseEntity<List<Response>> getPostList(
      @RequestParam String keyWord,
      @RequestParam PostStatus postStatus, //진행, 마감
      @RequestParam PostOrder postOrder, // 최신순, 투표 참여 인원순, 채팅 참여 인원순, 마감임박순
      @RequestParam SearchQueryType searchQueryType, // 제목 및 본문, 태그
      @RequestParam(defaultValue = "1") int pageNum) {

    // 제목 및 본문 검색
    if (searchQueryType == SearchQueryType.CONTENT) {
      if (postStatus == PostStatus.ONGOING) {
        // 진행 중인 고민
        return ResponseEntity.ok(postSearchService.ongoingSearchByContent(keyWord, pageNum, postOrder));
      } else {
        // 마감된 고민
        return ResponseEntity.ok(postSearchService.completedSearchByContent(keyWord, pageNum, postOrder));
      }
    // 태그 검색
    } else {
      if (postStatus == PostStatus.ONGOING) {
        // 진행 중인 고민
        return ResponseEntity.ok(postSearchService.ongoingSearchByTag(keyWord, pageNum, postOrder));
      } else {
        // 마감된 고민
        return ResponseEntity.ok(postSearchService.completedSearchByTag(keyWord, pageNum, postOrder));
      }
    }
  }
}
