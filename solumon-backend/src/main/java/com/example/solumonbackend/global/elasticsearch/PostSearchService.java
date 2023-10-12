package com.example.solumonbackend.global.elasticsearch;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.SearchException;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.type.PostOrder;
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
  public List<PostListDto.Response> searchOngoingPostsByContent(String keyword, int pageNum, PostOrder postOrder) {
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.boolQuery()
            .must(QueryBuilders.rangeQuery("endAt").gt(LocalDateTime.now()))
            .must(QueryBuilders.multiMatchQuery(keyword, "title", "content")))
        .withPageable(PageRequestCustom.of(pageNum, postOrder))
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
  public List<PostListDto.Response> searchCompletedPostsByContent(String keyword, int pageNum, PostOrder postOrder) {
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.boolQuery()
            .must(QueryBuilders.rangeQuery("endAt").lte(LocalDateTime.now()))
            .must(QueryBuilders.multiMatchQuery(keyword, "title", "content")))
        .withPageable(PageRequestCustom.of(pageNum, postOrder))
        .build();

    return elasticsearchRestTemplate.search(query, PostDocument.class)
        .stream().map(document -> PostListDto.Response
            .postDocumentToPostListResponse(document.getContent()))
        .collect(Collectors.toList());
  }

  public List<PostListDto.Response> searchOngoingPostsByTag(String keyword, Integer pageNum, PostOrder postOrder) {
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.boolQuery()
            .must(QueryBuilders.rangeQuery("endAt").gt(LocalDateTime.now()))
            .must(QueryBuilders.matchQuery("tags", keyword)))
        .withPageable(PageRequestCustom.of(pageNum, postOrder))
        .build();

    return elasticsearchRestTemplate.search(query, PostDocument.class)
        .stream().map(document -> PostListDto.Response
            .postDocumentToPostListResponse(document.getContent()))
        .collect(Collectors.toList());
  }

  public List<PostListDto.Response> searchCompletedPostsByTag(String keyword, Integer pageNum, PostOrder postOrder) {
    NativeSearchQuery query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.boolQuery()
            .must(QueryBuilders.rangeQuery("endAt").lte(LocalDateTime.now()))
            .must(QueryBuilders.matchQuery("tags", keyword)))
        .withPageable(PageRequestCustom.of(pageNum, postOrder))
        .build();

    return elasticsearchRestTemplate.search(query, PostDocument.class)
        .stream().map(document -> PostListDto.Response
            .postDocumentToPostListResponse(document.getContent()))
        .collect(Collectors.toList());
  }

  public void save(Post post, List<String> tags) {
    PostDocument save = postSearchRepository.save(PostDocument.createPostDocument(post, tags));
    log.debug("[PostService] save postDocument, Id : {}", post.getPostId());
  }

  public void update(Post post, List<String> tags) {
    PostDocument postDocument = postSearchRepository.findById(post.getPostId())
        .orElseThrow(() -> new SearchException(ErrorCode.NOT_FOUND_POST_DOCUMENT));

    PostDocument save = postSearchRepository.save(postDocument.updatePostDocument(post, tags));
    log.debug("[PostService] update postDocument, Id : {}", postDocument.getId());
  }

  public void delete(Post post) {
    PostDocument postDocument = postSearchRepository.findById(post.getPostId())
        .orElseThrow(() -> new SearchException(ErrorCode.NOT_FOUND_POST_DOCUMENT));
    log.debug("[PostService] delete postDocument, Id : {}", postDocument.getId());
    postSearchRepository.delete(postDocument);
  }

  public void deleteAll() {
    postSearchRepository.deleteAll();
  }

  public Iterable<PostDocument> getAllData() {
    return postSearchRepository.findAll();
  }

  public void updateVoteCount(int voteCount, Long postId) {
    PostDocument postDocument = postSearchRepository.findById(postId)
        .orElseThrow(() -> new SearchException(ErrorCode.NOT_FOUND_POST_DOCUMENT));

    postDocument.setVoteCount(voteCount);
    postSearchRepository.save(postDocument);
  }
}
