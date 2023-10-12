package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.global.elasticsearch.PostDocument;
import com.example.solumonbackend.global.elasticsearch.PostSearchService;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.SearchException;
import com.example.solumonbackend.member.model.MemberDetail;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostDetailDto;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.model.PostListDto.Response;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.service.PostService;
import com.example.solumonbackend.post.service.RecommendationService;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import com.example.solumonbackend.post.type.PostType;
import com.example.solumonbackend.post.type.SearchQueryType;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;
  private final PostSearchService postSearchService;
  private final RecommendationService recommendationService;

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
      return ResponseEntity.ok(postService.getGeneralPostList(postStatus, postOrder, pageNum));
    } else {
      return ResponseEntity.ok(
          recommendationService.recommendBasedOnInterest(
              memberDetail.getMember(), postStatus, postOrder, PageRequestCustom.of(pageNum)));
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
        return ResponseEntity.ok(postSearchService.searchOngoingPostsByContent(keyWord, pageNum, postOrder));
      } else {
        // 마감된 고민
        if (postOrder == PostOrder.IMMINENT_CLOSE) {
          throw new SearchException(ErrorCode.CLOSED_DOCUMENT_FETCH_DISALLOWED);
        }
        return ResponseEntity.ok(postSearchService.searchCompletedPostsByContent(keyWord, pageNum, postOrder));
      }
    // 태그 검색
    } else {
      if (postStatus == PostStatus.ONGOING) {
        // 진행 중인 고민
        return ResponseEntity.ok(postSearchService.searchOngoingPostsByTag(keyWord, pageNum, postOrder));
      } else {
        // 마감된 고민
        if (postOrder == PostOrder.IMMINENT_CLOSE) {
          throw new SearchException(ErrorCode.CLOSED_DOCUMENT_FETCH_DISALLOWED);
        }
        return ResponseEntity.ok(postSearchService.searchCompletedPostsByTag(keyWord, pageNum, postOrder));
      }
    }
  }

  // 테스트용 엘라스틱 서치 인덱스 내 데이터 전체 삭제
  // TODO: 나중에 완성하면 지우기
  @DeleteMapping("/elasticsearch")
  public ResponseEntity<String> elasticSearchDeleteAll() {
    postSearchService.deleteAll();
    return ResponseEntity.ok("엘라스틱 서치 전체 삭제");
  }

  // 테스트용 엘라스틱 서치 인덱스 내 데이터 전체 조회
  // TODO: 나중에 완성하면 지우기
  @GetMapping("/search/all-data")
  public Iterable<PostDocument> getElasticsearchAllData() {
    return postSearchService.getAllData();
  }
}
