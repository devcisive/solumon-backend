package com.example.solumonbackend.global.elasticsearch;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.global.exception.SearchException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Post;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@ExtendWith(MockitoExtension.class)
class PostSearchServiceTest {

  PostDocument savedPostDocument;
  Member testMember;
  @Mock
  private PostSearchRepository postSearchRepository;
  @Mock
  private ElasticsearchRestTemplate elasticsearchRestTemplate;
  @InjectMocks
  private PostSearchService postSearchService;

  @BeforeEach
  public void setUp() {
    savedPostDocument = PostDocument.builder()
        .id(200L)
        .title("제목200")
        .content("내용200")
        .writer("테스트유저")
        .tags(List.of("생일", "선물", "생일선물"))
        .voteCount(0)
        .chatCount(0)
        .imageUrl(
            "https://solumon.s3.ap-northeast-2.amazonaws.com/post/dab65045-f35ftestImage1.jpg")
        .endAt("2023-10-20T17:00:00")
        .createdAt("2023-10-08T17:55:23")
        .build();

    testMember = Member.builder()
        .memberId(5L)
        .kakaoId(null)
        .email("zerobase@test.com")
        .password("password123!@#")
        .nickname("테스트유저")
        .role(MemberRole.GENERAL)
        .isFirstLogIn(true).build();
  }

  @DisplayName("elastic search 저장 성공")
  @Test
  void save() {
    // given
    Post mockPost = Post.builder()
        .postId(200L)
        .member(testMember)
        .title("제목200")
        .contents("내용200")
        .thumbnailUrl(
            "https://solumon.s3.ap-northeast-2.amazonaws.com/post/dab65045-f35ftestImage1.jpg")
        .voteCount(1)
        .chatCount(2)
        .endAt(LocalDateTime.parse("2023-10-10T17:41:22", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .createdAt(LocalDateTime.now())
        .build();

    List<String> mockTags = Arrays.asList("감자", "고구마", "구황작물");

    // when
    doReturn(savedPostDocument).when(postSearchRepository).save(any(PostDocument.class));

    ArgumentCaptor<PostDocument> captor = ArgumentCaptor.forClass(PostDocument.class);

    postSearchService.save(mockPost, mockTags);

    // then
    verify(postSearchRepository, times(1)).save(captor.capture());

    assertEquals("제목200", captor.getValue().getTitle());
    assertEquals(testMember.getNickname(), captor.getValue().getWriter());
    assertEquals("내용200", captor.getValue().getContent());
    assertEquals(mockTags.toString(), captor.getValue().getTags());

  }

  @DisplayName("elastic search 수정 성공")
  @Test
  void update() {
    // given
    Post updatePost = Post.builder()
        .postId(200L)
        .member(testMember)
        .thumbnailUrl(
            "https://solumon.s3.ap-northeast-2.amazonaws.com/post/dab65045-f35ftestImage1.jpg")
        .title("테스트 제목입니다.")
        .contents("테스트 본문 입니다.")
        .endAt(LocalDateTime.parse("2023-10-20T17:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .createdAt(
            LocalDateTime.parse("2023-10-08T17:55:23", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .voteCount(3)
        .chatCount(0)
        .build();

    List<String> updateTags = Arrays.asList("감자", "고구마", "구황작물");

    PostDocument updatePostDocument = PostDocument.builder()
        .id(200L)
        .title("테스트 제목입니다2.")
        .content("테스트 본문 입니다.")
        .writer("테스트유저")
        .tags(List.of("생일", "선물", "생일선물"))
        .voteCount(0)
        .chatCount(0)
        .imageUrl(
            "https://solumon.s3.ap-northeast-2.amazonaws.com/post/dab65045-f35ftestImage1.jpg")
        .endAt("2023-10-20T17:00:00")
        .createdAt("2023-10-08T17:55:23")
        .build();

    doReturn(Optional.of(savedPostDocument)).when(postSearchRepository)
        .findById(updatePost.getPostId());
    doReturn(updatePostDocument).when(postSearchRepository)
        .save(savedPostDocument.updatePostDocument(updatePost, updateTags));

    // when
    postSearchService.update(updatePost, updateTags);

    ArgumentCaptor<PostDocument> captor = ArgumentCaptor.forClass(PostDocument.class);

    // then
    verify(postSearchRepository, times(1)).findById(200L);
    verify(postSearchRepository, times(1)).save(captor.capture());
    assertEquals("테스트 제목입니다.", captor.getValue().getTitle());
    assertEquals("[감자, 고구마, 구황작물]", captor.getValue().getTags());
  }

  @DisplayName("elastic search 수정 실패 - 존재하지 않는 post document ID")
  @Test
  void updateFail_NOT_FOUND_POST_DOCUMENT() {
    // given
    Post updatePost = Post.builder()
        .postId(130L)
        .member(testMember)
        .thumbnailUrl(
            "https://solumon.s3.ap-northeast-2.amazonaws.com/post/dab65045-f35ftestImage1.jpg")
        .title("테스트 제목입니다.")
        .contents("테스트 본문 입니다.")
        .endAt(LocalDateTime.parse("2023-10-20T17:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .createdAt(
            LocalDateTime.parse("2023-10-08T17:55:23", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .voteCount(3)
        .chatCount(0)
        .build();

    List<String> updateTags = Arrays.asList("감자", "고구마", "구황작물");

    doReturn(Optional.empty()).when(postSearchRepository)
        .findById(updatePost.getPostId());

    // when
    SearchException exception = assertThrows(SearchException.class,
        () -> postSearchService.update(updatePost, updateTags));


    // then
    verify(postSearchRepository, times(1)).findById(updatePost.getPostId());
    Assertions.assertEquals(ErrorCode.NOT_FOUND_POST_DOCUMENT, exception.getErrorCode());

  }

  @DisplayName("elastic search 삭제 성공")
  @Test
  void delete() {
    // given
    Post mockPost = Post.builder()
        .postId(200L)
        .member(testMember)
        .title("제목200")
        .contents("내용200")
        .endAt(LocalDateTime.parse("2023-10-10T17:41:22", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .build();

    doReturn(Optional.of(savedPostDocument)).when(postSearchRepository).findById(mockPost.getPostId());

    // when
    postSearchService.delete(mockPost);

    // then
    verify(postSearchRepository, times(1)).findById(200L);
    verify(postSearchRepository, times(1)).delete(savedPostDocument);
  }

  @DisplayName("elastic search 삭제 실패 - 존재하지 않는 post document ID")
  @Test
  void deleteFail_NOT_FOUND_POST_DOCUMENT() {
    // given
    Post mockPost = Post.builder()
        .postId(130L)
        .member(testMember)
        .title("제목200")
        .contents("내용200")
        .endAt(LocalDateTime.parse("2023-10-10T17:41:22", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .build();

    when(postSearchRepository.findById(130L)).thenReturn(Optional.empty());

    //when
    SearchException exception = assertThrows(SearchException.class,
        () -> postSearchService.delete(mockPost));

    //then
    Assertions.assertEquals(ErrorCode.NOT_FOUND_POST_DOCUMENT, exception.getErrorCode());
  }

}