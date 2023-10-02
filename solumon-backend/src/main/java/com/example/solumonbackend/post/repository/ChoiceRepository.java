package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Choice;
import com.example.solumonbackend.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long> {

  void deleteAllByPost(Post post);

}
