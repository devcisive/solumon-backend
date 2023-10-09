package com.example.solumonbackend.post.service;

import com.example.solumonbackend.global.elasticsearch.PostSearchService;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.common.AwsS3Component;
import com.example.solumonbackend.post.entity.Choice;
import com.example.solumonbackend.post.entity.Image;
import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Tag;
import com.example.solumonbackend.post.model.AwsS3;
import com.example.solumonbackend.post.model.PageRequestCustom;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostDetailDto;
import com.example.solumonbackend.post.model.PostDto.ChoiceDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.example.solumonbackend.post.model.PostDto.VoteResultDto;
import com.example.solumonbackend.post.model.PostListDto;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.repository.ChoiceRepository;
import com.example.solumonbackend.post.repository.ImageRepository;
import com.example.solumonbackend.post.repository.PostRepository;
import com.example.solumonbackend.post.repository.PostTagRepository;
import com.example.solumonbackend.post.repository.TagRepository;
import com.example.solumonbackend.post.repository.VoteRepository;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final ImageRepository imageRepository;
  private final AwsS3Component awsS3Component;
  private final TagRepository tagRepository;
  private final PostTagRepository postTagRepository;
  private final ChoiceRepository choiceRepository;
  private final VoteRepository voteRepository;
  private final PostSearchService postSearchService;

  private final String POST_DIR = "post";

  // TODO : 대표 이미지 넘어오는 방식 정해지면 저장방식 수정
  @Transactional
  public PostAddDto.Response createPost(Member member, PostAddDto.Request request,
                                        List<MultipartFile> images) {
    Post post = postRepository.save(Post.builder()
        .member(member)
        .title(request.getTitle())
        .contents(request.getContents())
        .endAt(request.getVote().getEndAt())
        .build());

    List<PostTag> savePostTags = savePostTag(request.getTags(), post);
    List<Choice> saveChoices = saveChoices(request.getVote().getChoices(), post);
    List<Image> saveImages = saveImages(images, post);

    postSearchService.save(post, request.getTags()
        .stream().map(tag -> tag.getTag())
        .collect(Collectors.toList()));

    return PostAddDto.Response.postToResponse(post, savePostTags, saveChoices, saveImages);
  }

  private List<Image> saveImages(List<MultipartFile> images, Post post) {
    // 이미지 파일이 없다면 빈 리스트 리턴(NullPointException 방지 차원)
    if (images.isEmpty()) {
      return List.of();
    }

    // s3에 저장 후 key와 imageUrl 값을 가진 AwsS3를 리스트에 저장
    List<AwsS3> awsS3List = new ArrayList<>();
    for (MultipartFile image : images) {
      try {
        awsS3List.add(awsS3Component.upload(image, POST_DIR));
      } catch (IOException e) {
        throw new PostException(ErrorCode.IMAGE_CAN_NOT_SAVE);
      }
    }

    return imageRepository.saveAll(awsS3List.stream()
        .filter(Objects::nonNull)
        .map(s3 -> Image.builder()
            .post(post)
            .imageKey(s3.getKey())
            .imageUrl(s3.getPath())
            .build())
        .collect(Collectors.toList()));
  }

  private List<Choice> saveChoices(List<ChoiceDto> choices, Post post) {
    return choiceRepository.saveAll(choices.stream()
        .map(choice -> Choice.builder()
            .post(post)
            .choiceNum(choice.getChoiceNum())
            .choiceText(choice.getChoiceText())
            .build())
        .collect(Collectors.toList()));
  }

  private List<PostTag> savePostTag(List<TagDto> tags, Post post) {
    // Tag 테이블에 저장된 것이 아니라면 Tag에 먼저 저장, 저장된 거라면 태그이름으로 찾와서 PostTag 테이블에 저장
    for (TagDto tagDto : tags) {
      Tag tag;
      if (tagRepository.existsByName(tagDto.getTag())) {
        tag = tagRepository.findByName(tagDto.getTag())
            .orElseThrow(() -> new PostException(ErrorCode.NOT_FOUND_TAG));
      } else {
        tag = tagRepository.save(Tag.builder()
            .name(tagDto.getTag())
            .build());
      }

      postTagRepository.save(PostTag.builder()
          .post(post)
          .tag(tag)
          .build());
    }

    return postTagRepository.findAllByPost_PostId(post.getPostId());
  }

  // TODO : 채팅 부분 추가
  public PostDetailDto.Response getPostDetail(Member member, long postId) {
    Post post = getPost(postId);

    List<PostTag> tags = postTagRepository.findAllByPost_PostId(postId);
    List<Image> images = imageRepository.findAllByPost_PostId(postId);
    VoteResultDto voteResultDto = getVoteResultDto(member, post);

    return PostDetailDto.Response.postToResponse(post, tags, images, voteResultDto);
  }

  private VoteResultDto getVoteResultDto(Member member, Post post) {
    // 글쓴이거나 투표를 했거나 투표 기간이 지나면 결과접근 true 상태로 표시
    if (Objects.equals(post.getMember().getMemberId(), member.getMemberId())
        || voteRepository.existsByPost_PostIdAndMember_MemberId(post.getPostId(), member.getMemberId())
        || post.getEndAt().isBefore(LocalDateTime.now())) {
      return VoteResultDto.builder()
          .resultAccessStatus(true)
          .choices(voteRepository.getChoiceResults(post.getPostId()))
          .build();
    } else {
      return VoteResultDto.builder()
          .resultAccessStatus(false)
          .choices(voteRepository.getChoiceResults(post.getPostId()))
          .build();
    }
  }

  @Transactional
  public PostUpdateDto.Response updatePost(Member member, long postId, PostUpdateDto.Request request,
                                           List<MultipartFile> images) {
    Post post = getPost(postId);
    validatePostWriter(member, post);

    post.setTitle(request.getTitle());
    post.setContents(request.getContents());
    postRepository.save(post);

    postSearchService.update(post, request.getTags()
        .stream().map(tag -> tag.getTag())
        .collect(Collectors.toList()));

    postTagRepository.deleteAllByPost_PostId(postId);
    savePostTag(request.getTags(), post);

    try {
      List<Image> imageList = updateImages(post, images);
      return PostUpdateDto.Response.postToResponse(post, request.getTags(), imageList);

    } catch (IOException e) {
      throw new PostException(ErrorCode.IMAGE_CAN_NOT_SAVE);
    }
  }

  private List<Image> updateImages(Post post, List<MultipartFile> images) throws IOException {
    deleteImage(post.getPostId());

    if (images.isEmpty()) {
      return List.of();
    }

    return saveImages(images, post);
  }

  @Transactional
  public void deletePost(Member member, long postId) {
    Post post = getPost(postId);
    validatePostWriter(member, post);

    deleteImage(postId);
    postTagRepository.deleteAllByPost_PostId(postId);
    voteRepository.deleteAllByPost_PostId(postId);
    choiceRepository.deleteAllByPost_PostId(postId);
    postRepository.deleteById(postId);

    postSearchService.delete(post);
  }

  private void deleteImage(long postId) {
    // 해당 post에 이미지가 있다면 s3와 image 테이블에서 삭제
    List<Image> imageList = imageRepository.findAllByPost_PostId(postId);
    if (!imageList.isEmpty()) {
      awsS3Component.removeAll(imageList.stream()
          .map(image -> AwsS3.builder()
              .key(image.getImageKey())
              .path(image.getImageUrl())
              .build())
          .collect(Collectors.toList()));
      imageRepository.deleteAll(imageList);
    }
  }

  private Post getPost(long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new PostException(ErrorCode.NOT_FOUND_POST));
  }

  private void validatePostWriter(Member member, Post post) {
    if (!Objects.equals(post.getMember().getMemberId(), member.getMemberId())) {
      throw new PostException(ErrorCode.ONLY_AVAILABLE_TO_THE_WRITER);
    }
  }


  public Page<PostListDto.Response> getGeneralPostList(PostStatus postStatus, PostOrder postOrder, Integer pageNum) {

    Pageable pageable = PageRequestCustom.of(pageNum, postOrder);
    // postRepository 와 연결된 PostRepositoryCustom 내의 메소드 호출
    return postRepository.getGeneralPostList(postStatus, postOrder, pageable);
  }
}
