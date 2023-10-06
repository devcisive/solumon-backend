package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.PostTag;
import com.example.solumonbackend.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

  List<PostTag> findAllByPost_PostId(Long postId);

  void deleteAllByPost_PostId(Long postId);

  List<PostTag> findDistinctByTagIn(List<Tag> tags);

}
