package com.example.solumonbackend.post.service;

import com.example.solumonbackend.chat.model.ChatMessageDto;
import com.example.solumonbackend.chat.repository.ChannelMemberRepository;
import com.example.solumonbackend.chat.repository.ChatMessageRepository;
import com.example.solumonbackend.global.elasticsearch.PostSearchService;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.PostException;
import com.example.solumonbackend.global.exception.TagException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.common.AwsS3Component;
import com.example.solumonbackend.post.entity.*;
import com.example.solumonbackend.post.model.*;
import com.example.solumonbackend.post.model.PostDto.ChoiceDto;
import com.example.solumonbackend.post.model.PostDto.ImageDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.example.solumonbackend.post.model.PostDto.VoteResultDto;
import com.example.solumonbackend.post.repository.*;
import com.example.solumonbackend.post.type.PostOrder;
import com.example.solumonbackend.post.type.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
  private final VoteCustomRepository voteCustomRepository;
  private final ChannelMemberRepository channelMemberRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final PostSearchService postSearchService;

  private final String POST_DIR = "post";

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
    List<Image> saveImages = saveImages(images, post, request.getImages());

    postSearchService.save(post, request.getTags()
        .stream().map(tag -> tag.getTag())
        .collect(Collectors.toList()));

    return PostAddDto.Response.postToResponse(post, savePostTags, saveChoices, saveImages);
  }

  private List<Image> saveImages(List<MultipartFile> images, Post post, List<ImageDto> imageDtoList) {
    // 이미지 파일이 없다면 빈 리스트 리턴(NullPointException 방지 차원)
    // TODO : 이미지가 없을 때 기본이미지를 대표이미지로 설정
    if (images.isEmpty()) {
      return List.of();
    }

    // 순서별 이미지파일명만 가져오기
    List<String> imageNameList = imageDtoList.stream()
        .map(ImageDto::getImage)
        .collect(Collectors.toList());

    // 대표이미지 인덱스 찾기
    int representIdx = 0;
    for (ImageDto dto : imageDtoList) {
      if (dto.isRepresentative()) {
        representIdx = dto.getIndex() - 1;
        break;
      }
    }

    // s3에 저장 후 key와 imageUrl 값을 가진 AwsS3를 배열에 저장
    // 이미지가 최대 5개밖에 없고 이미지 순서가 뒤죽박죽으로 온다면 순서를 맞춰 리스트에 저장이 어려울 것 같아 배열을 선택
    AwsS3[] awsS3Array = new AwsS3[images.size()];
    for (MultipartFile image : images) {
      try {
        AwsS3 upload = awsS3Component.upload(image, POST_DIR);

        // 파일명과 일치하는 인덱스 찾은 후 배열에 넣음
        int idx = imageNameList.indexOf(image.getOriginalFilename());
        awsS3Array[idx] = upload;

        // 인덱스가 대표이미지 인덱스와 일치한다면 post에 저장
        if (idx == representIdx) {
          post.setThumbnailUrl(upload.getPath());
          postRepository.save(post);
        }

      } catch (IOException e) {
        throw new PostException(ErrorCode.IMAGE_CAN_NOT_SAVE);
      }
    }

    return imageRepository.saveAll(Arrays.stream(awsS3Array)
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
    // 태그가 없다면 빈 리스트 리턴(NullPointException 방지 차원)
    if (tags.isEmpty()) {
      return List.of();
    }

    // Tag 테이블에 저장된 것이 아니라면 Tag에 먼저 저장, 저장된 거라면 태그이름으로 찾와서 PostTag 테이블에 저장
    for (TagDto tagDto : tags) {
      Tag tag;
      if (tagRepository.existsByName(tagDto.getTag())) {
        tag = tagRepository.findByName(tagDto.getTag())
            .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG, tagDto.getTag()));
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


  public PostDetailDto.Response getPostDetail(Member member, long postId, Long lastChatMessageId) {
    Post post = getPost(postId);

    List<PostTag> tags = postTagRepository.findAllByPost_PostId(postId);
    List<Image> images = imageRepository.findAllByPost_PostId(postId);
    VoteResultDto voteResultDto = getVoteResultDto(member, post);


    // 해당 게시글의 쌓인 채팅메세지 내역 가져오기
    Slice<ChatMessageDto.Response> lastChatMessages
        = chatMessageRepository.getLastChatMessagesScroll(postId,lastChatMessageId ,Pageable.ofSize(10));


    return PostDetailDto.Response.postToResponse(post, tags, images, voteResultDto, lastChatMessages);
  }

  private VoteResultDto getVoteResultDto(Member member, Post post) {
    // 글쓴이거나 투표를 했거나 투표 기간이 지나면 결과접근 true 상태로 표시
    if (Objects.equals(post.getMember().getMemberId(), member.getMemberId())
        || post.getEndAt().isBefore(LocalDateTime.now())
        || voteRepository.existsByPost_PostIdAndMember_MemberId(post.getPostId(), member.getMemberId())) {
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
    List<PostTag> postTagList = savePostTag(request.getTags(), post);

    try {
      List<Image> imageList = updateImages(post, images, request.getImages());
      return PostUpdateDto.Response.postToResponse(post, postTagList, imageList);

    } catch (IOException e) {
      throw new PostException(ErrorCode.IMAGE_CAN_NOT_SAVE);
    }
  }

  private List<Image> updateImages(Post post, List<MultipartFile> images,
                                   List<ImageDto> imageDtoList) throws IOException {
    deleteImage(post.getPostId());

    if (images.isEmpty()) {
      return List.of();
    }

    return saveImages(images, post, imageDtoList);
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
