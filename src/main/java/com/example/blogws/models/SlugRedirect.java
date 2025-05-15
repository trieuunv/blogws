package com.example.blogws.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SlugRedirect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oldSlug;
    private String newSlug;

    public SlugRedirect() {
    }

    public SlugRedirect(String oldSlug, String newSlug) {
        this.oldSlug = oldSlug;
        this.newSlug = newSlug;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOldSlug() {
        return oldSlug;
    }

    public void setOldSlug(String oldSlug) {
        this.oldSlug = oldSlug;
    }

    public String getNewSlug() {
        return newSlug;
    }

    public void setNewSlug(String newSlug) {
        this.newSlug = newSlug;
    }

}
