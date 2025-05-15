package com.example.blogws.controllers;

import com.example.blogws.models.Category;
import com.example.blogws.models.Post;
import com.example.blogws.services.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    private final PostService postService;

    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String home(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postService.getAllPublishedPosts(pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("recentPosts", postService.getRecentPosts());
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());

        return "pages/home";
    }

    @GetMapping("/category/{id}")
    public String postsByCategory(@PathVariable Long id,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Category category = postService.getCategoryById(id);
        Page<Post> posts = postService.getPostsByCategory(id, pageable);

        model.addAttribute("category", category);
        model.addAttribute("posts", posts);
        model.addAttribute("recentPosts", postService.getRecentPosts());
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());

        return "pages/category";
    }

    @GetMapping("/my-posts")
    public String myPosts(Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (principal == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postService.getPostsByCurrentUser(principal.getName(), pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());

        return "pages/my-posts";
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam String keyword,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postService.searchPostsByTitle(keyword, pageable);

        model.addAttribute("keyword", keyword);
        model.addAttribute("posts", posts);
        model.addAttribute("recentPosts", postService.getRecentPosts());
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());

        return "pages/search";
    }
}