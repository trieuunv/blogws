package com.example.blogws.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.blogws.models.SectionImage;

@Repository
public interface SectionImageRepository extends JpaRepository<SectionImage, Long> {
}
