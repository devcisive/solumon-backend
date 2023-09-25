package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom { // 쿼리 dsl 사용을 위해 상속클래스 추가

}
