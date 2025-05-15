package com.example.blogws.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.blogws.models.Category;
import com.example.blogws.models.Post;
import com.example.blogws.models.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategory(Category category);

    Page<Post> findByCategoryAndIsPublishedTrue(Category category, Pageable pageable);

    List<Post> findByAuthor(User author);

    Page<Post> findByAuthorAndIsPublishedTrue(User author, Pageable pageable);

    Page<Post> findByAuthor(User author, Pageable pageable);

    Optional<Post> findBySlug(String slug);

    Page<Post> findByIsPublishedTrue(Pageable pageable);

    List<Post> findTop5ByIsPublishedTrueOrderByCreatedAtDesc();

    Page<Post> findByTitleContainingIgnoreCaseAndIsPublishedTrue(String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.sections WHERE p.id = :id")
    Optional<Post> findByIdWithSections(@Param("id") Long id);

}