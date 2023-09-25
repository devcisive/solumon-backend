package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Post;
import com.example.solumonbackend.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

  List<PostTag> findAllByPost(Post post);

  void deleteAllByPost(Post post);

}
