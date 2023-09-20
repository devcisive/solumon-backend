package com.example.solumonbackend.post.repository;

import com.example.solumonbackend.post.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findByName(String interest);

}
