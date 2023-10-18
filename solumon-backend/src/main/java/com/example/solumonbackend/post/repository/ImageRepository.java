package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableJpaRepositories
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

  List<Image> findAllByPost_PostId(Long postId);

}
