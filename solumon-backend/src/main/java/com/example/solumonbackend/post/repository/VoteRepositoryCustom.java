package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.model.PostDto;

import java.util.List;

public interface VoteRepositoryCustom {

  List<PostDto.ChoiceResultDto> getChoiceResults(Long postId);

}