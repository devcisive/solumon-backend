package com.example.solumonbackend.post.service;

import com.example.solumonbackend.global.elasticsearch.PostSearchService;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.common.AwsS3Component;
import com.example.solumonbackend.post.entity.Choice;
import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.*;
import com.example.solumonbackend.post.repository.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @Mock
  private PostRepository postRepository;
  @Mock
  private ImageRepository imageRepository;
  @Mock
  private AwsS3Component awsS3Component;
  @Mock
  private TagRepository tagRepository;
  @Mock
  private PostTagRepository postTagRepository;
  @Mock
  private ChoiceRepository choiceRepository;
  @Mock
  private VoteRepository voteRepository;
  @Mock
  private VoteRepositoryCustomImpl voteCustomRepository;
  @Mock
  private PostSearchService postSearchService;

  @InjectMocks
  private PostService postService;

  @BeforeEach
  public void setUp() {
    postMember = Member.builder()
        .memberId(1L)
        .email("test@gmail.com")
        .nickname("별명")
        .role(MemberRole.GENERAL)
        .build();

    mockPost = Post.builder()
        .postId(1L)
        .title("제목")
        .contents("내용")
        .member(postMember)
        .endAt(LocalDateTime.of(2023, 9, 28, 10, 0, 0)
            .plusDays(2))
        .build();
  }

  Member postMember;
  Post mockPost;

  @Test
  @DisplayName("게시글 작성 성공")
  void createPost_success() throws IOException {
    //given
    PostAddDto.Request request = getAddRequest();
    List<MultipartFile> images = new ArrayList<>();
    images.add(new MockMultipartFile("images", "image1.jpg",
        "image/jpeg", "image data".getBytes()));

    List<String> tags = request.getTags()
        .stream().map(tag -> tag.getTag())
        .collect(Collectors.toList());

    List<AwsS3> awsS3List = List.of(AwsS3.builder()
        .key("dirName/image1.jpg")
        .path("imageUrl")
        .build());

    List<Choice> choices = List.of(
        Choice.builder()
            .choiceId(1L)
            .post(mockPost)
            .choiceNum(1)
            .choiceText("선택1")
            .build(),
        Choice.builder()
            .choiceId(2L)
            .post(mockPost)
            .choiceNum(2)
            .choiceText("선택2")
            .build());

    when(postRepository.save(any(Post.class)))
        .thenReturn(mockPost);
    when(postTagRepository.findAllByPost_PostId(mockPost.getPostId()))
        .thenReturn(List.of(
            PostTag.builder()
                .postTagId(1L)
                .tag(Tag.builder().tagId(1L).name("태그1").build())
                .post(mockPost)
                .build(),
            PostTag.builder()
                .postTagId(2L)
                .tag(Tag.builder().tagId(2L).name("태그2").build())
                .post(mockPost)
                .build()));
    when(choiceRepository.saveAll(anyList()))
        .thenReturn(choices);
    when(awsS3Component.upload(images.get(0), "post"))
        .thenReturn(AwsS3.builder()
            .key("dirName/image1.jpg")
            .path("imageUrl")
            .build());
    when(imageRepository.saveAll(anyList()))
        .thenReturn(List.of(new Image(1L, mockPost,
            "dirName/image1.jpg", "imageUrl")));

    //when
    PostAddDto.Response response = postService.createPost(postMember, request, images);
    System.out.println(images);

    ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
    ArgumentCaptor<List> elasticTags = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<PostTag> postTagArgumentCaptor = ArgumentCaptor.forClass(PostTag.class);
    ArgumentCaptor<Tag> tagArgumentCaptor = ArgumentCaptor.forClass(Tag.class);

    //then
    assertThat(response.getPostId()).isEqualTo(1L);
    assertThat(response.getTags().get(0).getTag()).isEqualTo("태그1");
    assertThat(response.getWriter()).isEqualTo(postMember.getNickname());
    assertThat(response.getImages().size()).isEqualTo(1);

    verify(postSearchService, times(1)).save(postArgumentCaptor.capture(), elasticTags.capture());
    verify(postRepository, times(1)).save(postArgumentCaptor.capture());
    verify(postTagRepository, times(2)).save(postTagArgumentCaptor.capture());
    verify(tagRepository, times(2)).save(tagArgumentCaptor.capture());
    verify(choiceRepository, times(1)).saveAll(anyList());
    verify(awsS3Component, times(1)).upload(images.get(0), "post");
    verify(imageRepository, times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("게시글 작성 성공 - 이미지 없을 때")
  void createPost_success_noImages() throws IOException {
    //given
    PostAddDto.Request request = getAddRequest();

    List<MultipartFile> images = new ArrayList<>();

    when(postRepository.save(any(Post.class)))
        .thenReturn(mockPost);
    when(postTagRepository.findAllByPost_PostId(1L))
        .thenReturn(List.of(
            PostTag.builder()
                .postTagId(1L)
                .tag(Tag.builder().tagId(1L).name("태그1").build())
                .post(mockPost)
                .build(),
            PostTag.builder()
                .postTagId(2L)
                .tag(Tag.builder().tagId(2L).name("태그2").build())
                .post(mockPost)
                .build()));

    //when
    PostAddDto.Response response = postService.createPost(postMember, request, images);

    //then
    assertThat(response.getPostId()).isEqualTo(1L);
    assertThat(response.getTags().get(0).getTag()).isEqualTo("태그1");
    assertThat(response.getWriter()).isEqualTo(postMember.getNickname());

    verify(postRepository, times(1)).save(any(Post.class));
    verify(postTagRepository, times(2)).save(any(PostTag.class));
    verify(tagRepository, times(2)).save(any(Tag.class));
    verify(choiceRepository, times(1)).saveAll(anyList());
    verify(awsS3Component, times(0)).upload(any(), eq("post"));
    verify(imageRepository, times(0)).saveAll(anyList());
  }

  @Test
  @DisplayName("게시글 상세 조회 성공")
  void getPostDetail_success() {
    //given
    when(postRepository.findById(1L))
        .thenReturn(Optional.of(mockPost));
    when(postTagRepository.findAllByPost_PostId(1L))
        .thenReturn(List.of(
            new PostTag(1L, mockPost,
                Tag.builder()
                    .tagId(1L)
                    .name("태그1")
                    .build()),
            new PostTag(2L, mockPost,
                Tag.builder()
                    .tagId(2L)
                    .name("태그2")
                    .build())));
    when(imageRepository.findAllByPost_PostId(1L))
        .thenReturn(List.of(
            Image.builder()
                .imageId(1L)
                .post(mockPost)
                .imageKey("dir/image1.jpg")
                .imageUrl("imageUrl1")
                .build(),
            Image.builder()
                .imageId(2L)
                .post(mockPost)
                .imageKey("dir/image2.jpg")
                .imageUrl("imageUrl2")
                .build()));
    when(voteCustomRepository.getChoiceResults(1L))
        .thenReturn(List.of(
            PostDto.ChoiceResultDto.builder()
                .choiceNum(1)
                .choiceText("선택1")
                .choiceCount(5L)
                .choicePercent(50L).build(),
            PostDto.ChoiceResultDto.builder()
                .choiceNum(2)
                .choiceText("선택2")
                .choiceCount(5L)
                .choicePercent(50L).build()));

    //when
    PostDetailDto.Response response = postService.getPostDetail(postMember, 1);

    //then
    assertThat(response.getPostId()).isEqualTo(1L);
    assertThat(response.getTags().get(0).getTag()).isEqualTo("태그1");
    assertThat(response.getWriter()).isEqualTo(postMember.getNickname());
    assertThat(response.getImages().get(1).getImage()).isEqualTo("imageUrl2");
    assertThat(response.getVoteCount()).isEqualTo(10);

    verify(postRepository, times(1)).findById(1L);
    verify(postTagRepository, times(1)).findAllByPost_PostId(1L);
    verify(imageRepository, times(1)).findAllByPost_PostId(1L);
    verify(voteRepository, times(1)).existsByPost_PostIdAndMember_MemberId(1L, 1L);
    verify(voteCustomRepository, times(1)).getChoiceResults(1L);
  }

  @Test
  @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
  void getPostDetail_fail_notFoundPost() {
    //given
    when(postRepository.findById(2L))
        .thenReturn(Optional.empty());

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.getPostDetail(postMember, 2));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("게시글 수정 성공")
  void updatePost_success() throws IOException {
    //given
    PostUpdateDto.Request request = getUpdateRequest();

    List<MultipartFile> images = new ArrayList<>();
    images.add(new MockMultipartFile("images", "image2.jpg",
        "image2/jpeg", "image data".getBytes()));

    Post updatePost = mockPost;
    updatePost.setTitle(request.getTitle());
    updatePost.setContents(request.getContents());

    when(postRepository.findById(1L))
        .thenReturn(Optional.of(mockPost));
    when(postRepository.save(any(Post.class)))
        .thenReturn(Post.builder()
            .postId(1L)
            .title("제목2")
            .contents("내용2")
            .member(postMember)
            .endAt(mockPost.getEndAt())
            .build());
    when(imageRepository.saveAll(anyList()))
        .thenReturn(List.of(
            Image.builder()
                .imageId(1L)
                .post(updatePost)
                .imageKey("dir/image2.jpg")
                .imageUrl("imageUrl2")
                .build()));

    //when
    PostUpdateDto.Response response = postService.updatePost(postMember, 1, request, images);

    //then
    assertThat(response.getPostId()).isEqualTo(1L);
    assertThat(response.getTags().get(0).getTag()).isEqualTo("태그2");
    assertThat(response.getWriter()).isEqualTo(postMember.getNickname());
    assertThat(response.getImages().get(0).getImage()).isEqualTo("imageUrl2");

    verify(postRepository, times(1)).findById(1L);
    verify(postRepository, times(1)).save(any(Post.class));
    verify(postTagRepository, times(1)).deleteAllByPost_PostId(1L);
    verify(postTagRepository, times(2)).save(any(PostTag.class));
    verify(imageRepository, times(1)).findAllByPost_PostId(1L);
    verify(awsS3Component, times(1)).upload(images.get(0), "post");
    verify(imageRepository, times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
  void updatePost_fail_notFoundPost() {
    //given
    PostUpdateDto.Request request = getUpdateRequest();
    List<MultipartFile> images = new ArrayList<>();

    when(postRepository.findById(2L))
        .thenReturn(Optional.empty());

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.updatePost(postMember, 2, request, images));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("게시글 수정 실패 - 작성자가 아닌 회원")
  void updatePost_fail_onlyAvailableWriter() {
    //given
    Member otherMember = Member.builder().memberId(2L).build();
    PostUpdateDto.Request request = getUpdateRequest();
    List<MultipartFile> images = new ArrayList<>();

    when(postRepository.findById(1L))
        .thenReturn(Optional.of(mockPost));

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.updatePost(otherMember, 1, request, images));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ONLY_AVAILABLE_TO_THE_WRITER);
  }

  @Test
  @DisplayName("게시글 삭제 성공")
  void deletePost_success() {
    //given
    when(postRepository.findById(1L))
        .thenReturn(Optional.of(mockPost));
    when(imageRepository.findAllByPost_PostId(1L))
        .thenReturn(List.of(new Image(1L, mockPost,
            "dir/image1.jpg", "imageUrl")));

    //when
    postService.deletePost(postMember, 1L);

    //then
    verify(postRepository, times(1)).findById(1L);
    verify(imageRepository, times(1)).findAllByPost_PostId(1L);
    verify(awsS3Component, times(1)).removeAll(anyList());
    verify(imageRepository, times(1)).deleteAll(anyList());
    verify(postTagRepository, times(1)).deleteAllByPost_PostId(1L);
    verify(voteRepository, times(1)).deleteAllByPost_PostId(1L);
    verify(choiceRepository, times(1)).deleteAllByPost_PostId(1L);
    verify(postRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("게시글 삭제 성공 - 이미지 없는 경우")
  void deletePost_success_noImages() {
    //given
    when(postRepository.findById(1L))
        .thenReturn(Optional.of(mockPost));
    when(imageRepository.findAllByPost_PostId(1L))
        .thenReturn(List.of());

    //when
    postService.deletePost(postMember, 1L);

    //then
    verify(postRepository, times(1)).findById(1L);
    verify(imageRepository, times(1)).findAllByPost_PostId(1L);
    verify(awsS3Component, times(0)).removeAll(anyList());
    verify(imageRepository, times(0)).deleteAll(anyList());
    verify(postTagRepository, times(1)).deleteAllByPost_PostId(1L);
    verify(voteRepository, times(1)).deleteAllByPost_PostId(1L);
    verify(choiceRepository, times(1)).deleteAllByPost_PostId(1L);
    verify(postRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
  void deletePost_fail_notFoundPost() {
    //given
    when(postRepository.findById(2L))
        .thenReturn(Optional.empty());

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.deletePost(postMember, 2));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("게시글 삭제 실패 - 작성자가 아닌 회원")
  void deletePost_fail_onlyAvailableWriter() {
    //given
    Member otherMember = Member.builder().memberId(2L).build();

    when(postRepository.findById(1L))
        .thenReturn(Optional.of(mockPost));

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.deletePost(otherMember, 1));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ONLY_AVAILABLE_TO_THE_WRITER);
  }

  private static PostAddDto.Request getAddRequest() {
    return PostAddDto.Request.builder()
        .title("제목")
        .contents("내용")
        .tags(List.of(new PostDto.TagDto("태그1"), new PostDto.TagDto("태그2")))
        .vote(PostDto.VoteDto.builder()
            .choices(List.of(new PostDto.ChoiceDto(1, "선택1")
                , new PostDto.ChoiceDto(2, "선택2")))
            .endAt(LocalDateTime.of(2023, 9, 28, 10, 0, 0)
                .plusDays(2))
            .build())
        .build();
  }

  private static PostUpdateDto.Request getUpdateRequest() {
    return PostUpdateDto.Request.builder()
        .title("제목2")
        .contents("내용2")
        .tags(List.of(new PostDto.TagDto("태그2"), new PostDto.TagDto("태그3")))
        .build();
  }


}