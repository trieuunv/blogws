package com.example.blogws.models;

import jakarta.persistence.*;

@Entity
@Table(name = "section_videos")
public class SectionVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoUrl;
    private String caption;
    private int position;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private PostSection section;

    public SectionVideo(String videoUrl, String caption, int position, PostSection section) {
        this.videoUrl = videoUrl;
        this.caption = caption;
        this.position = position;
        this.section = section;
    }

    public SectionVideo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
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
