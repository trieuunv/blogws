package com.example.blogws.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.blogws.models.SlugRedirect;

public interface SlugRedirectRepository extends JpaRepository<SlugRedirect, Long> {
    Optional<SlugRedirect> findByOldSlug(String oldSlug);
}
