package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.entity.RefreshToken;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.repository.RefreshTokenRedisRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostDto;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.repository.ImageRepository;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private RefreshTokenRedisRepository refreshTokenRedisRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private ImageRepository imageRepository;
  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private PostTagRepository postTagRepository;

  @BeforeEach
  public void setUp() {
    member = Member.builder()
        .email("test@gmail.com")
        .nickname("별명")
        .role(MemberRole.GENERAL)
        .build();

    memberRepository.save(member);

    savePost = postRepository.save(Post.builder()
        .member(member)
        .title("제목")
        .contents("내용")
        .endAt(LocalDateTime.of(2023, 9, 30, 13, 22, 32))
        .build());
    System.out.println(savePost.getPostId());

    Tag tag = tagRepository.save(Tag.builder().name("태그1").build());
    postTagRepository.save(PostTag.builder().post(savePost).tag(tag).build());
  }

  @AfterEach
  public void cleanUp() {
    memberRepository.deleteAll();
    refreshTokenRedisRepository.deleteAll();
    postTagRepository.deleteAll();
    tagRepository.deleteAll();
    postRepository.deleteAll();
  }

  Member member;
  Post savePost;

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 작성")
  void createPost() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    PostAddDto.Request request = addRequest();
    String jsonRequest = objectMapper.writeValueAsString(request);

    // Mock 파일 생성
    FileInputStream fileInputStream = new FileInputStream("src/test/testImage/testImage1.jpg");
    MockMultipartFile image1 = new MockMultipartFile(
        "images", // name
        "testImage1.jpg", // originalFilename
        "jpg", // content-type
        fileInputStream
    );
    fileInputStream.close();

    //when
    //then
    mockMvc.perform(multipart("/posts")
            .file(image1)
            .file(new MockMultipartFile("request", "",
                "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8)))
            .contentType("multipart/form-data")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken")
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value(request.getTitle()))
        .andExpect(jsonPath("$.tags[0].tag").value(request.getTags().get(0).getTag()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 상세 조회")
  void getPostDetail() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    //when
    //then
    // GenerationType이 identity라 테스트할 때마다 저장되는 postId가 달라짐 -> savePost.getPostId()를 사용
    mockMvc.perform(get("/posts/" + savePost.getPostId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("제목"));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
  void getPostDetail_fail_notFoundPost() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    //when
    //then
    mockMvc.perform(get("/posts/" + (savePost.getPostId() + 1))
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_POST.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 수정")
  void updatePost() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));
    imageRepository.save(Image.builder()
        .post(savePost)
        // 게시글 추가했을 때 저장했던 이미지 key, url : 테스트 시 s3에 접근하기 때문에 s3에 저장된 걸로 변경해야함
        .imageKey("post/453cc5a0-1058-43ea-8db5-f8ef5c5bec9btestImage1.jpg")
        .imageUrl("https://solumon.s3.ap-northeast-2.amazonaws.com/post/453cc5a0-1058-43ea-8db5-f8ef5c5bec9btestImage1.jpg").build());

    PostUpdateDto.Request request = PostUpdateDto.Request.builder()
        .title("제목2")
        .contents("내용")
        .tags(List.of(new PostDto.TagDto("태그2"), new PostDto.TagDto("태그3")))
        .build();
    String jsonRequest = objectMapper.writeValueAsString(request);

    // Mock 파일 생성
    FileInputStream fileInputStream = new FileInputStream("src/test/testImage/testImage1.jpg");
    MockMultipartFile image1 = new MockMultipartFile(
        "images", // name
        "testImage1.jpg", // originalFilename
        "jpg", // content-type
        fileInputStream
    );
    fileInputStream.close();

    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders
            .multipart(HttpMethod.PUT, "/posts/" + savePost.getPostId())
            .file(image1)
            .file(new MockMultipartFile("request", "",
                "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8)))
            .contentType("multipart/form-data")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken")
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value(request.getTitle()))
        .andExpect(jsonPath("$.contents").value(request.getContents()))
        .andExpect(jsonPath("$.tags[0].tag").value(request.getTags().get(0).getTag()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 수정 - 저장된 이미지 없을 때")
  void updatePost_noSavedImages() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    PostUpdateDto.Request request = PostUpdateDto.Request.builder()
        .title("제목2")
        .contents("내용")
        .tags(List.of(new PostDto.TagDto("태그2"), new PostDto.TagDto("태그3")))
        .build();
    String jsonRequest = objectMapper.writeValueAsString(request);

    // Mock 파일 생성
    FileInputStream fileInputStream = new FileInputStream("src/test/testImage/testImage1.jpg");
    MockMultipartFile image1 = new MockMultipartFile(
        "images", // name
        "testImage1.jpg", // originalFilename
        "jpg", // content-type
        fileInputStream
    );
    fileInputStream.close();

    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders
            .multipart(HttpMethod.PUT, "/posts/" + savePost.getPostId())
            .file(image1)
            .file(new MockMultipartFile("request", "",
                "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8)))
            .contentType("multipart/form-data")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken")
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value(request.getTitle()))
        .andExpect(jsonPath("$.contents").value(request.getContents()))
        .andExpect(jsonPath("$.tags[0].tag").value(request.getTags().get(0).getTag()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
  void updatePost_fail_notFoundPost() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    PostUpdateDto.Request request = PostUpdateDto.Request.builder()
        .title("제목2")
        .contents("내용")
        .tags(List.of(new PostDto.TagDto("태그2"), new PostDto.TagDto("태그3")))
        .build();
    String jsonRequest = objectMapper.writeValueAsString(request);

    // Mock 파일 생성
    FileInputStream fileInputStream = new FileInputStream("src/test/testImage/testImage1.jpg");
    MockMultipartFile image1 = new MockMultipartFile(
        "images", // name
        "testImage1.jpg", // originalFilename
        "jpg", // content-type
        fileInputStream
    );
    fileInputStream.close();

    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders
            .multipart(HttpMethod.PUT, "/posts/" + (savePost.getPostId() + 1))
            .file(image1)
            .file(new MockMultipartFile("request", "",
                "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8)))
            .contentType("multipart/form-data")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken")
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_POST.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 수정 실패 - 작성자만 가능")
  void updatePost_fail_onlyAvailableToWriter() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    Member otherMember = memberRepository.save(Member.builder()
        .email("test2@gmail.com")
        .nickname("별명2")
        .role(MemberRole.GENERAL)
        .build());
    Post otherPost = postRepository.save(Post.builder()
        .member(otherMember)
        .title("제목")
        .contents("내용")
        .build());

    PostUpdateDto.Request request = PostUpdateDto.Request.builder()
        .title("제목2")
        .contents("내용")
        .tags(List.of(new PostDto.TagDto("태그2"), new PostDto.TagDto("태그3")))
        .build();
    String jsonRequest = objectMapper.writeValueAsString(request);

    // Mock 파일 생성
    FileInputStream fileInputStream = new FileInputStream("src/test/testImage/testImage1.jpg");
    MockMultipartFile image1 = new MockMultipartFile(
        "images", // name
        "testImage1.jpg", // originalFilename
        "jpg", // content-type
        fileInputStream
    );
    fileInputStream.close();

    //when
    //then
    mockMvc.perform(MockMvcRequestBuilders
            .multipart(HttpMethod.PUT, "/posts/" + otherPost.getPostId())
            .file(image1)
            .file(new MockMultipartFile("request", "",
                "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8)))
            .contentType("multipart/form-data")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken")
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ONLY_AVAILABLE_TO_THE_WRITER.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 삭제")
  void deletePost() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));
    imageRepository.save(Image.builder()
        .post(savePost)
        // 게시글 추가했을 때 저장했던 이미지 key, url : 테스트 시 s3에 접근하기 때문에 s3에 저장된 걸로 변경해야함
        .imageKey("post/91f17176-88b8-4570-a6b3-5e9c64e1594d공룡곰.png")
        .imageUrl("https://solumon.s3.ap-northeast-2.amazonaws.com/post/91f17176-88b8-4570-a6b3-5e9c64e1594d%EA%B3%B5%EB%A3%A1%EA%B3%B0.png").build());

    //when
    //then
    mockMvc.perform(delete("/posts/" + savePost.getPostId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("게시글이 삭제되었습니다."));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 삭제 - 저장된 이미지 없을 때")
  void deletePost_noSavedImages() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    //when
    //then
    mockMvc.perform(delete("/posts/" + savePost.getPostId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("게시글이 삭제되었습니다."));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
  void deletePost_fail_notFoundPost() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    //when
    //then
    mockMvc.perform(delete("/posts/" + (savePost.getPostId() + 1))
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_POST.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 삭제 실패 - 작성자만 가능")
  void deletePost_fail_onlyAvailableToWriter() throws Exception {
    //given
    refreshTokenRedisRepository.save(new RefreshToken("accessToken", "refreshToken"));

    Member otherMember = memberRepository.save(Member.builder()
        .email("test2@gmail.com")
        .nickname("별명2")
        .role(MemberRole.GENERAL)
        .build());
    Post otherPost = postRepository.save(Post.builder()
        .member(otherMember)
        .title("제목")
        .contents("내용")
        .build());

    //when
    //then
    mockMvc.perform(delete("/posts/" + otherPost.getPostId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-AUTH-TOKEN", "accessToken"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ONLY_AVAILABLE_TO_THE_WRITER.toString()));
  }

  private PostAddDto.Request addRequest() {
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

}