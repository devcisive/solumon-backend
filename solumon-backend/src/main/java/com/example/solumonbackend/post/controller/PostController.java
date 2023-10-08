package com.example.solumonbackend.post.controller;
import com.example.solumonbackend.global.elasticsearch.PostDocument;
import com.example.solumonbackend.global.elasticsearch.PostSearchService;
import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.model.PostListDto.Response;
import com.example.solumonbackend.post.model.PostDetailDto;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.service.PostService;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import com.example.solumonbackend.post.type.PostType;
import com.example.solumonbackend.post.type.SearchQueryType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchType;
import org.springframework.data.domain.Page;
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
  private final PostSearchService postSearchService;

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

  // 일반 조회
  @GetMapping
  public ResponseEntity<Page<PostListDto.Response>> getPostList(@AuthenticationPrincipal MemberDetail memberDetail,
      @RequestParam PostType postType,
      @RequestParam PostStatus postStatus,
      @RequestParam PostOrder postOrder,
      @RequestParam(defaultValue = "1") Integer pageNum) {


    if (postType == PostType.GENERAL) {
      Page<Response> generalPostList = postService.getGeneralPostList(postStatus, postOrder,
          pageNum);
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

  // 테스트용 엘라스틱 서치 인덱스 내 데이터 전체 삭제
  // 나중에 완성하면 지우기
  @DeleteMapping("/elasticsearch")
  public ResponseEntity<String> elasticSearchDeleteAll() {
    postSearchService.deleteAll();
    return ResponseEntity.ok("엘라스틱 서치 전체 삭제");
  }

  // 테스트용 엘라스틱 서치 인덱스 내 데이터 전체 조회
  // 나중에 완성하면 지우기
  @GetMapping("/search/all-data")
  public Iterable<PostDocument> getElasticsearchAllData() {
    return postSearchService.getAllData();
  }
}
