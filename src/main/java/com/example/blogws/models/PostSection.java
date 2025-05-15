package com.example.blogws.models;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "post_sections")
public class PostSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sectionTitle;

    @Lob
    private String content;

    private int position;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<SectionImage> images;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<SectionVideo> videos;

    public PostSection() {
    }

    public PostSection(String sectionTitle, String content, int position, Post post, List<SectionImage> images,
            List<SectionVideo> videos) {
        this.sectionTitle = sectionTitle;
        this.content = content;
        this.position = position;
        this.post = post;
        this.images = images;
        this.videos = videos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public List<SectionImage> getImages() {
        return images;
    }

    public void setImages(List<SectionImage> images) {
        this.images = images;
    }

    public List<SectionVideo> getVideos() {
        return videos;
    }

    public void setVideos(List<SectionVideo> videos) {
        this.videos = videos;
    }
}
