package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

  List<PostTag> findAllByPost_PostId(Long postId);

  void deleteAllByPost_PostId(Long postId);
  List<PostTag> findDistinctByTagIn(List<Tag> tags);
  List<PostTag> findDistinctByTagInAndPostModifiedAtIsAfter(List<Tag> tags, LocalDateTime localDateTime);
  List<PostTag> findDistinctByTagInAndPostCreatedAtIsAfter(List<Tag> tags, LocalDateTime localDateTime);


}
