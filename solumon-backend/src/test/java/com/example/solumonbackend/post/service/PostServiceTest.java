package com.example.solumonbackend.post.service;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.*;
import com.example.solumonbackend.post.repository.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @Mock
  private PostRepository postRepository;
  @Mock
  private ImageRepository imageRepository;
  @Mock
  private AwsS3Service awsS3Service;
  @Mock
  private TagRepository tagRepository;
  @Mock
  private PostTagRepository postTagRepository;
  @Mock
  private ChoiceRepository choiceRepository;
  @Mock
  private VoteRepository voteRepository;
  @Mock
  private VoteCustomRepository voteCustomRepository;

  @InjectMocks
  private PostService postService;

  @Test
  @DisplayName("게시글 작성")
  void createPost() throws IOException {
    //given
    Member member = member();
    PostAddDto.Request request = postAddRequest();
    Post post = post(member);
    List<MultipartFile> images = new ArrayList<>();
    images.add(new MockMultipartFile("images", "image1.jpg",
        "image/jpeg", "image data".getBytes()));

    when(postRepository.save(any(Post.class))).thenReturn(post);
    when(postTagRepository.findAllByPost_PostId(any())).thenReturn(List.of(
        PostTag.builder()
            .postTagId(1L)
            .tag(Tag.builder().tagId(1L).name("태그1").build())
            .post(post)
            .build(),
        PostTag.builder()
            .postTagId(2L)
            .tag(Tag.builder().tagId(2L).name("태그2").build())
            .post(post)
            .build()));
    when(awsS3Service.upload(any(MultipartFile.class), anyString()))
        .thenReturn(AwsS3.builder()
            .key("key")
            .path("path")
            .build());
    when(imageRepository.saveAll(anyList()))
        .thenReturn(List.of(new Image(1L, post, "key", "path")));

    //when
    PostAddDto.Response response = postService.createPost(member, request, images);

    //then
    assertThat(response.getPostId()).isEqualTo(1l);
    assertThat(response.getTags().get(0).getTag()).isEqualTo("태그1");
    assertThat(response.getWriter()).isEqualTo(member.getNickname());
    assertThat(response.getImages().get(0).getImage()).isEqualTo("path");

    verify(postRepository, times(1)).save(any(Post.class));
    verify(postTagRepository, times(2)).save(any(PostTag.class));
    verify(tagRepository, times(2)).save(any(Tag.class));
    verify(choiceRepository, times(1)).saveAll(anyList());
    verify(awsS3Service, times(1)).upload(any(MultipartFile.class), anyString());
    verify(imageRepository, times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("게시글 작성 - 이미지 없을 때")
  void createPost_noImages() throws IOException {
    //given
    Member member = member();
    PostAddDto.Request request = postAddRequest();
    Post post = post(member);
    List<MultipartFile> images = new ArrayList<>();

    when(postRepository.save(any(Post.class))).thenReturn(post);
    when(postTagRepository.findAllByPost_PostId(any())).thenReturn(List.of(
        PostTag.builder()
            .postTagId(1L)
            .tag(Tag.builder().tagId(1L).name("태그1").build())
            .post(post)
            .build(),
        PostTag.builder()
            .postTagId(2L)
            .tag(Tag.builder().tagId(2L).name("태그2").build())
            .post(post)
            .build()));

    //when
    PostAddDto.Response response = postService.createPost(member, request, images);

    //then
    assertThat(response.getPostId()).isEqualTo(1l);
    assertThat(response.getTags().get(0).getTag()).isEqualTo("태그1");
    assertThat(response.getWriter()).isEqualTo(member.getNickname());

    verify(postRepository, times(1)).save(any(Post.class));
    verify(postTagRepository, times(2)).save(any(PostTag.class));
    verify(tagRepository, times(2)).save(any(Tag.class));
    verify(choiceRepository, times(1)).saveAll(anyList());
    verify(awsS3Service, times(0)).upload(any(MultipartFile.class), anyString());
    verify(imageRepository, times(0)).saveAll(anyList());
  }

  @Test
  @DisplayName("게시글 상세 조회")
  void getPostDetail() {
    //given
    Member member = member();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member))); // getPost
    when(postTagRepository.findAllByPost_PostId(anyLong()))
        .thenReturn(postTagList(member)); // getPostTagList
    when(imageRepository.findAllByPost_PostId(anyLong()))
        .thenReturn(imageList(post(member))); // getImageList
    when(voteCustomRepository.getChoiceResults(anyLong()))
        .thenReturn(choiceResultDtoList()); // getChoiceResultDtoList

    //when
    PostDetailDto.Response response = postService.getPostDetail(member, 1);

    //then
    assertThat(response.getPostId()).isEqualTo(1L);
    assertThat(response.getTags().get(0).getTag()).isEqualTo("태그1");
    assertThat(response.getWriter()).isEqualTo(member.getNickname());
    assertThat(response.getImages().get(1).getImage()).isEqualTo("url2");
    assertThat(response.getVoteCount()).isEqualTo(10);

    verify(postRepository, times(1)).findById(anyLong());
    verify(postTagRepository, times(1)).findAllByPost_PostId(anyLong());
    verify(imageRepository, times(1)).findAllByPost_PostId(anyLong());
    verify(voteRepository, times(1)).existsByPost_PostIdAndMember_MemberId(anyLong(), anyLong());
    verify(voteCustomRepository, times(1)).getChoiceResults(anyLong());
  }

  @Test
  @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
  void getPostDetail_fail_notFoundPost() {
    //given
    Member member = member();

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.getPostDetail(member, 2));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("게시글 수정")
  void updatePost() throws IOException {
    //given
    Member member = member();
    PostUpdateDto.Request request = PostUpdateDto.Request.builder()
        .title("제목2")
        .contents("내용2")
        .tags(List.of(new PostDto.TagDto("태그2"), new PostDto.TagDto("태그3")))
        .build();
    List<MultipartFile> images = new ArrayList<>();
    images.add(new MockMultipartFile("images", "image2.jpg",
        "image2/jpeg", "image data".getBytes()));
    Post updatePost = post(member);
    updatePost.setTitle(request.getTitle());
    updatePost.setContents(request.getContents());

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member)));
    when(postRepository.save(any(Post.class)))
        .thenReturn(updatePost);
    when(imageRepository.saveAll(anyList()))
        .thenReturn(List.of(new Image(1L, updatePost, "key2", "url2")));
    when(voteRepository.countByPost_PostId(anyLong()))
        .thenReturn(10);

    //when
    PostUpdateDto.Response response = postService.updatePost(member, 1, request, images);

    //then
    assertThat(response.getPostId()).isEqualTo(1L);
    assertThat(response.getTags().get(0).getTag()).isEqualTo("태그2");
    assertThat(response.getWriter()).isEqualTo(member.getNickname());
    assertThat(response.getImages().get(0).getImage()).isEqualTo("url2");
    assertThat(response.getVoteCount()).isEqualTo(10);

    verify(postRepository, times(1)).findById(anyLong());
    verify(postRepository, times(1)).save(any(Post.class));
    verify(postTagRepository, times(1)).deleteAllByPost_PostId(anyLong());
    verify(postTagRepository, times(2)).save(any(PostTag.class));
    verify(imageRepository, times(1)).findAllByPost_PostId(anyLong());
    verify(awsS3Service, times(1)).upload(any(MultipartFile.class), anyString());
    verify(imageRepository, times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
  void updatePost_fail_notFoundPost() {
    //given
    PostUpdateDto.Request request = PostUpdateDto.Request.builder()
        .title("제목2")
        .contents("내용2")
        .tags(List.of(new PostDto.TagDto("태그2"), new PostDto.TagDto("태그3")))
        .build();
    List<MultipartFile> images = new ArrayList<>();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.updatePost(member(), 1, request, images));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("게시글 수정 실패 - 작성자가 아닌 회원")
  void updatePost_fail_onlyAvailableWriter() {
    //given
    Member member2 = Member.builder().memberId(2L).build();
    PostUpdateDto.Request request = PostUpdateDto.Request.builder()
        .title("제목2")
        .contents("내용2")
        .tags(List.of(new PostDto.TagDto("태그2"), new PostDto.TagDto("태그3")))
        .build();
    List<MultipartFile> images = new ArrayList<>();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member())));

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.updatePost(member2, 1, request, images));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ONLY_AVAILABLE_TO_THE_WRITER);
  }

  @Test
  @DisplayName("게시글 삭제")
  void deletePost() {
    //given
    Member member = member();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member)));
    when(imageRepository.findAllByPost_PostId(anyLong()))
        .thenReturn(List.of(new Image(1L, post(member), "key1", "url1")));

    //when
    postService.deletePost(member, 1L);

    //then
    verify(postRepository, times(1)).findById(anyLong());
    verify(imageRepository, times(1)).findAllByPost_PostId(anyLong());
    verify(awsS3Service, times(1)).removeAll(anyList());
    verify(imageRepository, times(1)).deleteAll(anyList());
    verify(postTagRepository, times(1)).deleteAllByPost_PostId(anyLong());
    verify(voteRepository, times(1)).deleteAllByPost_PostId(anyLong());
    verify(choiceRepository, times(1)).deleteAllByPost_PostId(anyLong());
    verify(postRepository, times(1)).deleteById(anyLong());
  }

  @Test
  @DisplayName("게시글 삭제 - 이미지 없는 경우")
  void deletePost_noImages() {
    //given
    Member member = member();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member)));
    when(imageRepository.findAllByPost_PostId(anyLong()))
        .thenReturn(List.of());

    //when
    postService.deletePost(member, 1L);

    //then
    verify(postRepository, times(1)).findById(anyLong());
    verify(imageRepository, times(1)).findAllByPost_PostId(anyLong());
    verify(awsS3Service, times(0)).removeAll(anyList());
    verify(imageRepository, times(0)).deleteAll(anyList());
    verify(postTagRepository, times(1)).deleteAllByPost_PostId(anyLong());
    verify(voteRepository, times(1)).deleteAllByPost_PostId(anyLong());
    verify(choiceRepository, times(1)).deleteAllByPost_PostId(anyLong());
    verify(postRepository, times(1)).deleteById(anyLong());
  }

  @Test
  @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
  void deletePost_fail_notFoundPost() {
    //given
    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.deletePost(member(), 1));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_POST);
  }

  @Test
  @DisplayName("게시글 삭제 실패 - 작성자가 아닌 회원")
  void deletePost_fail_onlyAvailableWriter() {
    //given
    Member member2 = Member.builder().memberId(2L).build();

    when(postRepository.findById(anyLong()))
        .thenReturn(Optional.of(post(member())));

    //when
    PostException exception = Assertions.assertThrows(PostException.class,
        () -> postService.deletePost(member2, 1));

    //then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ONLY_AVAILABLE_TO_THE_WRITER);
  }

  private Post post(Member member) {
    return Post.builder()
        .postId(1L)
        .title("제목")
        .contents("내용")
        .member(member)
        .endAt(LocalDateTime.now().plusDays(3))
        .build();
  }

  private PostAddDto.Request postAddRequest() {
    return PostAddDto.Request.builder()
        .title("제목")
        .contents("내용")
        .tags(List.of(new PostDto.TagDto("태그1"), new PostDto.TagDto("태그2")))
        .vote(PostDto.VoteDto.builder()
            .choices(List.of(new PostDto.ChoiceDto(1, "선택1")
                , new PostDto.ChoiceDto(2, "선택2")))
            .endAt(LocalDateTime.now().plusDays(2)).build())
        .build();
  }

  private Member member() {
    return Member.builder()
        .memberId(1L)
        .email("test@gmail.com")
        .nickname("별명")
        .role(MemberRole.GENERAL)
        .build();
  }

  private List<PostDto.ChoiceResultDto> choiceResultDtoList() {
    return List.of(PostDto.ChoiceResultDto.builder()
            .choiceNum(1)
            .choiceText("선택1")
            .choiceCount(5L)
            .choicePercent(50).build(),
        PostDto.ChoiceResultDto.builder()
            .choiceNum(2)
            .choiceText("선택2")
            .choiceCount(5L)
            .choicePercent(50).build());
  }

  private List<Image> imageList(Post post) {
    return List.of(Image.builder()
            .imageId(1L)
            .post(post)
            .key("image1")
            .imageUrl("url1")
            .build(),
        Image.builder()
            .imageId(2L)
            .post(post)
            .key("image2")
            .imageUrl("url2")
            .build());
  }

  private List<PostTag> postTagList(Member member) {
    return List.of(
        new PostTag(1L, post(member),
            Tag.builder()
                .tagId(1L)
                .name("태그1")
                .build()),
        new PostTag(2L, post(member),
            Tag.builder()
                .tagId(2L)
                .name("태그2")
                .build()));
  }

}