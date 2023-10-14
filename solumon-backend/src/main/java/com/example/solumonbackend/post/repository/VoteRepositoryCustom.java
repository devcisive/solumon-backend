package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.model.PostDto.ChoiceResultDto;
import java.util.List;

public interface VoteRepositoryCustom {

  List<ChoiceResultDto> getChoiceResults(Long postId);

}
