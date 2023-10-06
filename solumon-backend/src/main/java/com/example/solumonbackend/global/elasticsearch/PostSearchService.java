package com.example.solumonbackend.global.elasticsearch;

import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.SearchQueryType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostSearchService {

  private final ElasticsearchRestTemplate elasticsearchRestTemplate;

  private final PostSearchRepository postSearchRepository;

  /**
   * 마감 안된 것 제목 및 본문 검색 오타, 비슷한 문자 포함.
   * @param keyword 검색할 단어
   * @param pageNum 페이지 번호
   * @param postOrder 정렬 기준 : 마감O, 마감X
   * @return 진행중인 항목들 중에서 검색한 단어가 제목 및 본문에 포함되는 리스트
   */
  // TODO : postOrder enum확인
  public List<PostListDto.Response> ongoingSearchByContent(String keyword, int pageNum, PostOrder postOrder) {
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.boolQuery()
            .must(QueryBuilders.rangeQuery("endAt").gt(LocalDateTime.now()))
            .must(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("title", keyword))
                .should(QueryBuilders.matchQuery("content", keyword))))
        .withPageable(PageRequestCustom.ofType(pageNum, postOrder))
        .build();

    return elasticsearchRestTemplate.search(query, PostDocument.class)
        .stream().map(document -> PostListDto.Response
            .postDocumentToPostListResponse(document.getContent()))
        .collect(Collectors.toList());
  }

  /**
   * 마감 된 것 제목 및 본문 검색 오타, 비슷한 문자 포함.
   *
   * @param keyword   검색할 단어
   * @param pageNum   페이지 번호
   * @param postOrder 정렬 기준 : 마감O, 마감X
   * @return 마감된 항목들 중에서 검색한 단어가 제목 및 본문에 포함되는 리스트
   */
  public List<PostListDto.Response> completedSearchByContent(String keyword, int pageNum, PostOrder postOrder) {
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.boolQuery()
            .must(QueryBuilders.rangeQuery("endAt").lte(LocalDateTime.now()))
            .must(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("title", keyword))
                .should(QueryBuilders.matchQuery("content", keyword))))
        .withPageable(PageRequestCustom.ofType(pageNum, postOrder))
        .build();

    return elasticsearchRestTemplate.search(query, PostDocument.class)
        .stream().map(document -> PostListDto.Response
            .postDocumentToPostListResponse(document.getContent()))
        .collect(Collectors.toList());
  }

  public List<PostListDto.Response> ongoingSearchByTag(String keyword, Integer pageNum, PostOrder postOrder) {
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.boolQuery()
            .must(QueryBuilders.rangeQuery("endAt").gt(LocalDateTime.now()))
            .must(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("tags", keyword))))
        .withPageable(PageRequestCustom.ofType(pageNum, postOrder))
        .build();

    return elasticsearchRestTemplate.search(query, PostDocument.class)
        .stream().map(document -> PostListDto.Response
            .postDocumentToPostListResponse(document.getContent()))
        .collect(Collectors.toList());
  }

  public List<PostListDto.Response> completedSearchByTag(String keyword, Integer pageNum, PostOrder postOrder) {
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.boolQuery()
            .must(QueryBuilders.rangeQuery("endAt").lte(LocalDateTime.now()))
            .must(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("tags", keyword))))
        .withPageable(PageRequestCustom.ofType(pageNum, postOrder))
        .build();

    return elasticsearchRestTemplate.search(query, PostDocument.class)
        .stream().map(document -> PostListDto.Response
            .postDocumentToPostListResponse(document.getContent()))
        .collect(Collectors.toList());
  }

  public void save(Post post, List<String> tags) {
    postSearchRepository.save(PostDocument.builder()
        .post(post)
        .tags(tags)
        .build());
    log.debug("[PostService] save postDocument, Id : {}", post.getPostId());
  }

  // TODO : exception설정
  public void update(Post post, List<String> tags) {
    PostDocument postDocument = postSearchRepository.findById(post.getPostId())
        .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
    postDocument.updatePostDocument(post, tags);
    postSearchRepository.save(postDocument);
    log.debug("[PostService] update postDocument, Id : {}", postDocument.getPostId());
  }

  // 삭제
  // TODO : exception설정
  public void delete(Post post) {
    PostDocument postDocument = postSearchRepository.findById(post.getPostId())
        .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
    log.debug("[PostService] delete postDocument, Id : {}", postDocument.getPostId());
    postSearchRepository.delete(postDocument);
  }

}
