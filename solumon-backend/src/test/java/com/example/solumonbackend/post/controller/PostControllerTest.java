package com.example.solumonbackend.post.controller;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import com.example.solumonbackend.member.type.MemberRole;
import com.example.solumonbackend.post.entity.*;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostDto;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private PostRepository postRepository;
  @Autowired
  private ImageRepository imageRepository;
  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private PostTagRepository postTagRepository;
  @Autowired
  private ChoiceRepository choiceRepository;

  Member member;
  Member otherMember;
  Post savePost;

  @BeforeEach
  public void setUp() {
    member = Member.builder()
        .email("test@gmail.com")
        .nickname("별명")
        .role(MemberRole.GENERAL)
        .build();
    otherMember = memberRepository.save(Member.builder()
        .email("test2@gmail.com")
        .nickname("별명2")
        .role(MemberRole.GENERAL)
        .build());
    memberRepository.saveAll(List.of(member, otherMember));

    savePost = postRepository.save(Post.builder()
        .member(member)
        .title("제목")
        .contents("내용")
        .endAt(LocalDateTime.of(2023, 9, 30, 13, 0, 0).plusDays(10))
        .build());

    Tag tag = tagRepository.save(Tag.builder().name("태그1").build());
    postTagRepository.save(PostTag.builder().post(savePost).tag(tag).build());

    choiceRepository.saveAll(List.of(
        Choice.builder()
            .choiceNum(1)
            .choiceText("잠자기")
            .post(savePost)
            .build(),
        Choice.builder()
            .choiceNum(2)
            .choiceText("나가기")
            .post(savePost)
            .build()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 작성 성공")
  void createPost_success() throws Exception {
    //given
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
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value(request.getTitle()))
        .andExpect(jsonPath("$.tags[0].tag").value(request.getTags().get(0).getTag()))
        .andExpect(jsonPath("$.vote.choices[0].choice_text")
            .value(request.getVote().getChoices().get(0).getChoiceText()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 상세 조회 성공")
  void getPostDetail_success() throws Exception {
    //given
    //when
    //then
    // GenerationType이 identity라 테스트할 때마다 저장되는 postId가 달라짐 -> savePost.getPostId()를 사용
    mockMvc.perform(get("/posts/" + savePost.getPostId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.post_id").value(savePost.getPostId()))
        .andExpect(jsonPath("$.title").value("제목"))
        .andExpect(jsonPath("$.contents").value("내용"))
        .andExpect(jsonPath("$.tags[0].tag").value("태그1"))
        .andExpect(jsonPath("$.vote.choices[0].choice_num").value(1))
        .andExpect(jsonPath("$.vote.choices[0].choice_percent").value(0));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
  void getPostDetail_fail_notFoundPost() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(get("/posts/" + (savePost.getPostId() + 1)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_POST.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 수정 성공")
  void updatePost_success() throws Exception {
    //given
    imageRepository.save(Image.builder()
        .post(savePost)
        // 게시글 추가했을 때 저장했던 이미지 key, url : 테스트 시 s3에 접근하기 때문에 s3에 저장된 걸로 변경해야함
        .imageKey("post/0341c6be-dc3a-4dff-9c92-3124a2188953testImage1.jpg")
        .imageUrl("https://solumon.s3.ap-northeast-2.amazonaws.com/post/0341c6be-dc3a-4dff-9c92-3124a2188953testImage1.jpg")
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
            .multipart(HttpMethod.PUT, "/posts/" + savePost.getPostId())
            .file(image1)
            .file(new MockMultipartFile("request", "",
                "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8)))
            .contentType("multipart/form-data")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value(request.getTitle()))
        .andExpect(jsonPath("$.contents").value(request.getContents()))
        .andExpect(jsonPath("$.tags[0].tag").value(request.getTags().get(0).getTag()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 수정 성공 - 저장된 이미지 없을 때")
  void updatePost_success_noSavedImages() throws Exception {
    //given
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
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_POST.toString()));
  }

  @Test
  @WithUserDetails(value = "test2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 수정 실패 - 작성자만 가능")
  void updatePost_fail_onlyAvailableToWriter() throws Exception {
    //given
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
            .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.ONLY_AVAILABLE_TO_THE_WRITER.toString()));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 삭제 성공")
  void deletePost_success() throws Exception {
    //given
    imageRepository.save(Image.builder()
        .post(savePost)
        // 게시글 추가했을 때 저장했던 이미지 key, url : 테스트 시 s3에 접근하기 때문에 s3에 저장된 걸로 변경해야함
        .imageKey("post/dab65045-f35f-4e5a-b51c-144f64ea2edetestImage1.jpg")
        .imageUrl("https://solumon.s3.ap-northeast-2.amazonaws.com/post/dab65045-f35f-4e5a-b51c-144f64ea2edetestImage1.jpg")
        .build());

    //when
    //then
    mockMvc.perform(delete("/posts/" + savePost.getPostId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("게시글이 삭제되었습니다."));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 삭제 성공 - 저장된 이미지 없을 때")
  void deletePost_success_noSavedImages() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(delete("/posts/" + savePost.getPostId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("게시글이 삭제되었습니다."));
  }

  @Test
  @WithUserDetails(value = "test@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
  void deletePost_fail_notFoundPost() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(delete("/posts/" + (savePost.getPostId() + 1)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_POST.toString()));
  }

  @Test
  @WithUserDetails(value = "test2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @DisplayName("게시글 삭제 실패 - 작성자만 가능")
  void deletePost_fail_onlyAvailableToWriter() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(delete("/posts/" + savePost.getPostId()))
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
            .endAt(LocalDateTime.of(2023, 9, 28, 14, 0, 0)
                .plusDays(10))
            .build())
        .build();
  }

}