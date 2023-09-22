package com.example.solumonbackend.post.service;

import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.post.entity.*;
import com.example.solumonbackend.post.model.PostAddDto;
import com.example.solumonbackend.post.model.PostDetailDto;
import com.example.solumonbackend.post.model.PostDto.ChoiceDto;
import com.example.solumonbackend.post.model.PostDto.TagDto;
import com.example.solumonbackend.post.model.PostDto.VoteResultDto;
import com.example.solumonbackend.post.model.PostUpdateDto;
import com.example.solumonbackend.post.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final ImageRepository imageRepository;

  private final TagRepository tagRepository;
  private final PostTagRepository postTagRepository;

  private final ChoiceRepository choiceRepository;
  private final VoteRepository voteRepository;
  private final VoteCustomRepository voteCustomRepository;

  // TODO : 이미지 s3 저장, image_url db 저장
  @Transactional
  public PostAddDto.Response createPost(Member member, PostAddDto.Request dto) {
    Post post = postRepository.save(Post.builder()
        .member(member)
        .title(dto.getTitle())
        .contents(dto.getContents())
        .endAt(dto.getVote().getEndAt())
        .build());

    savePostTag(dto.getTags(), post);
    saveChoices(dto.getVote().getChoices(), post);

    return PostAddDto.Response.postToResponse(post, dto.getTags(), dto.getVote());
  }

  private void saveChoices(List<ChoiceDto> choices, Post post) {
    for (ChoiceDto choice : choices) {
      choiceRepository.save(Choice.builder()
          .post(post)
          .choiceNum(choice.getChoiceNum())
          .choiceText(choice.getChoiceText())
          .build());
    }
  }

  private void savePostTag(List<TagDto> tags, Post post) {
    for (TagDto tagDto : tags) {
      Tag tag;
      if (tagRepository.existsByName(tagDto.getTag())) {
        tag = tagRepository.findByName(tagDto.getTag()).get();
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
  }

  // TODO : exception 수정, 채팅 부분 추가
  public PostDetailDto.Response getPostDetail(Member member, long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

    List<PostTag> tags = postTagRepository.findAllByPost(post);
    List<Image> images = imageRepository.findAllByPost(post);
    VoteResultDto voteResultDto = getVoteResultDto(member, post);

    return PostDetailDto.Response.postToResponse(post, tags, images, voteResultDto);
  }

  // TODO : voteCustomRepository.getChoiceResults() 쿼리 테스트 후 안되면 수정
  private VoteResultDto getVoteResultDto(Member member, Post post) {
    if (voteRepository.existsByPostAndMember(post, member)
        || post.getEndAt().isBefore(LocalDateTime.now())) {
      return VoteResultDto.builder()
          .resultAccessStatus(true)
          .choices(voteCustomRepository.getChoiceResults(post))
          .build();
    } else {
      return VoteResultDto.builder()
          .resultAccessStatus(false)
          .choices(voteCustomRepository.getChoiceResults(post))
          .build();
    }
  }

  // TODO : exception 수정, 이미지 수정 부분, voteCount, chatCount 추가
  @Transactional
  public PostUpdateDto.Response updatePost(Member member, long postId, PostUpdateDto.Request request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

    if (!post.getMember().equals(member)) {
      throw new RuntimeException("작성자만 수정이 가능합니다.");
    }

    post.setTitle(request.getTitle());
    post.setContents(request.getContents());
    postRepository.save(post);

    List<PostTag> tags = postTagRepository.findAllByPost(post);
    postTagRepository.deleteAll(tags);
    savePostTag(request.getTags(), post);

    return PostUpdateDto.Response.postToResponse(post, request.getTags(), request.getImages());
  }

  // TODO : exception 수정, 이미지 삭제 부분
  @Transactional
  public void deletePost(Member member, long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

    if (!post.getMember().equals(member)) {
      throw new RuntimeException("작성자만 삭제가 가능합니다.");
    }

    postTagRepository.deleteAllByPost(post);
    voteRepository.deleteAllByPost(post);
    choiceRepository.deleteAllByPost(post);
    postRepository.delete(post);
  }
}
