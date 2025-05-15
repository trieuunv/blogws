package com.example.blogws.models;

import jakarta.persistence.*;

@Entity
@Table(name = "section_images")
public class SectionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    private String caption;
    private int position;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private PostSection section;

    public SectionImage(String imageUrl, String caption, int position, PostSection section) {
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.position = position;
        this.section = section;
    }

    public SectionImage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public PostSection getSection() {
        return section;
    }

    public void setSection(PostSection section) {
        this.section = section;
    }

}
