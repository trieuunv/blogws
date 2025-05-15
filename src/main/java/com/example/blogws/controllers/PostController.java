package com.example.blogws.controllers;

import com.example.blogws.models.*;
import com.example.blogws.services.CommentService;
import com.example.blogws.services.PostService;
import com.example.blogws.services.UserService;
import com.example.blogws.utils.PostUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;

    public PostController(PostService postService, UserService userService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("categories", postService.getAllCategories());
        return "pages/create";
    }

    @PostMapping("/save")
    public String savePost(@ModelAttribute Post post,
            Principal principal,
            @RequestParam("sectionTitles") List<String> sectionTitles,
            @RequestParam("contents") List<String> contents,
            @RequestParam("positions") List<Integer> positions,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "imageCaptions", required = false) List<String> imageCaptions,
            @RequestParam(value = "imagePositions", required = false) List<Integer> imagePositions,
            @RequestParam(value = "videoUrls", required = false) List<String> videoUrls,
            @RequestParam(value = "videoCaptions", required = false) List<String> videoCaptions,
            @RequestParam(value = "videoPositions", required = false) List<Integer> videoPositions) {

        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setPublished(true);

        post.setSlug(PostUtils.generateSlug(post.getTitle()));

        List<PostSection> sections = new ArrayList<>();
        int imageIndex = 0;
        int videoIndex = 0;

        for (int i = 0; i < sectionTitles.size(); i++) {
            PostSection section = new PostSection();
            section.setSectionTitle(sectionTitles.get(i));
            section.setContent(contents.get(i));
            section.setPosition(positions.get(i));

            // Process images for this section
            List<SectionImage> images = new ArrayList<>();
            if (imageFiles != null) {
                while (imageIndex < imageFiles.size()) {
                    MultipartFile file = imageFiles.get(imageIndex);
                    String caption = imageCaptions != null && imageCaptions.size() > imageIndex
                            ? imageCaptions.get(imageIndex)
                            : "";
                    Integer position = imagePositions != null && imagePositions.size() > imageIndex
                            ? imagePositions.get(imageIndex)
                            : imageIndex + 1;

                    if (!file.isEmpty()) {
                        try {
                            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                            Path uploadPath = Paths.get("uploads/images/");
                            if (!Files.exists(uploadPath)) {
                                Files.createDirectories(uploadPath);
                            }
                            Path filePath = uploadPath.resolve(filename);
                            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                            SectionImage image = new SectionImage();
                            image.setImageUrl("/uploads/images/" + filename);
                            image.setCaption(caption);
                            image.setPosition(position);
                            images.add(image);

                        } catch (IOException e) {
                            e.printStackTrace(); // Or better: log this
                        }
                    }
                    imageIndex++;
                    // break if next image belongs to next section
                    if (imageIndex < imagePositions.size() && imagePositions.get(imageIndex) == 1) {
                        break;
                    }
                }
            }

            // Process videos for this section
            List<SectionVideo> videos = new ArrayList<>();
            if (videoUrls != null) {
                while (videoIndex < videoUrls.size()) {
                    String url = videoUrls.get(videoIndex);
                    String caption = videoCaptions != null && videoCaptions.size() > videoIndex
                            ? videoCaptions.get(videoIndex)
                            : "";
                    Integer position = videoPositions != null && videoPositions.size() > videoIndex
                            ? videoPositions.get(videoIndex)
                            : videoIndex + 1;

                    SectionVideo video = new SectionVideo();
                    video.setVideoUrl(url);
                    video.setCaption(caption);
                    video.setPosition(position);
                    videos.add(video);

                    videoIndex++;
                    // break if next video belongs to next section
                    if (videoIndex < videoPositions.size() && videoPositions.get(videoIndex) == 1) {
                        break;
                    }
                }
            }

            section.setImages(images);
            section.setVideos(videos);
            sections.add(section);
        }

        post.setSections(sections);

        for (PostSection section : sections) {
            List<SectionImage> imgs = section.getImages();
            if (imgs != null && !imgs.isEmpty()) {
                post.setThumbnailUrl(imgs.get(0).getImageUrl());
                break;
            }
        }

        postService.savePost(post, principal.getName());

        return "redirect:/posts/" + post.getSlug();
    }

    @GetMapping("/{slug}")
    public String viewPostBySlug(@PathVariable String slug, Model model) {
        Post post = postService.getPostBySlug(slug);
        model.addAttribute("post", post);
        model.addAttribute("recentPosts", postService.getRecentPosts());
        return "pages/posts/view";
    }

    // Comment
    @PostMapping("/{id}/comments(save)")
    public String saveComment(@PathVariable Long id, @RequestParam String content, Principal principal) {
        Post post = postService.getPostById(id);
        User user = userService.findByUsername(principal.getName()).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setCreatedAt(LocalDateTime.now());

        commentService.saveComment(comment);

        return "redirect:/posts/" + post.getSlug();
    }

    @GetMapping("/my-posts")
    public String viewMyPosts(Model model, Principal principal, Pageable pageable) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        Page<Post> posts = postService.getPostsByCurrentUser(user.getUsername(), pageable);
        model.addAttribute("posts", posts);
        return "pages/my-posts";
    }

    @GetMapping("/category/{id}")
    public String getCategoryPosts(@PathVariable Long id,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postService.getPostsByCategory(id, pageable);

        // Lấy thông tin category để hiển thị tên
        Category category = null;
        if (!posts.isEmpty()) {
            category = posts.getContent().get(0).getCategory();
        } else {
            // Trường hợp không có bài viết, vẫn cần lấy thông tin category
            try {
                category = postService.getCategoryById(id);
            } catch (RuntimeException e) {
                return "redirect:/"; // Chuyển hướng nếu category không tồn tại
            }
        }

        model.addAttribute("category", category);
        model.addAttribute("posts", posts);
        model.addAttribute("recentPosts", postService.getRecentPosts());
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());

        return "pages/category";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(
            Principal principal,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(principal.getName()).orElse(null);

        Post post = postService.getPostById(id);

        if (post == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy bài viết");
            return "redirect:/my-posts";
        }

        if (!post.getAuthor().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa bài viết này");
            return "redirect:/my-posts";
        }

        postService.deletePostById(id);
        redirectAttributes.addFlashAttribute("success", "Bài viết đã được xóa thành công");

        return "redirect:/my-posts";
    }

    // Hiển thị form chỉnh sửa
    @GetMapping("/edit/{slug}")
    public String editPostForm(@PathVariable String slug, Model model, Principal principal) {
        Post post = postService.getPostBySlug(slug);

        if (!post.getAuthor().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        model.addAttribute("post", post);
        model.addAttribute("categories", postService.getAllCategories());
        return "pages/posts/edit-post";
    }

    // Xử lý cập nhật bài viết
    @PostMapping("/update/{id}")
    public String updatePost(@PathVariable Long id,
            @ModelAttribute Post post,
            Principal principal,
            @RequestParam("sectionTitles") List<String> sectionTitles,
            @RequestParam("contents") List<String> contents,
            @RequestParam("positions") List<Integer> positions,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "imageCaptions", required = false) List<String> imageCaptions,
            @RequestParam(value = "imagePositions", required = false) List<Integer> imagePositions,
            @RequestParam(value = "videoUrls", required = false) List<String> videoUrls,
            @RequestParam(value = "videoCaptions", required = false) List<String> videoCaptions,
            @RequestParam(value = "videoPositions", required = false) List<Integer> videoPositions,
            @RequestParam(value = "existingSectionIds", required = false) List<Long> existingSectionIds,
            @RequestParam(value = "existingImageIds", required = false) List<Long> existingImageIds,
            @RequestParam(value = "existingVideoIds", required = false) List<Long> existingVideoIds,
            @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
            @RequestParam(value = "deletedVideoIds", required = false) List<Long> deletedVideoIds) {

        Post updatedPost = postService.updatePost(id, post, principal.getName(),
                sectionTitles, contents, positions,
                imageFiles, imageCaptions, imagePositions,
                videoUrls, videoCaptions, videoPositions,
                existingSectionIds, existingImageIds, existingVideoIds,
                deletedImageIds, deletedVideoIds);

        return "redirect:/posts/" + updatedPost.getSlug();
    }

    /*
     * @PostMapping("/save")
     * public String savePost(@ModelAttribute Post post,
     * Principal principal,
     * 
     * @RequestParam("sectionTitles") List<String> sectionTitles,
     * 
     * @RequestParam("contents") List<String> contents,
     * 
     * @RequestParam("positions") List<Integer> positions,
     * 
     * @RequestParam(value = "imageFiles", required = false) List<MultipartFile>
     * imageFiles,
     * 
     * @RequestParam(value = "imageCaptions", required = false) List<String>
     * imageCaptions,
     * 
     * @RequestParam(value = "videoUrls", required = false) List<String> videoUrls,
     * 
     * @RequestParam(value = "videoCaptions", required = false) List<String>
     * videoCaptions) {
     * 
     * // Set basic post info
     * post.setCreatedAt(LocalDateTime.now());
     * post.setUpdatedAt(LocalDateTime.now());
     * post.setPublished(true); // or set based on a checkbox in the form
     * 
     * // Generate slug from title if not provided
     * if (post.getSlug() == null || post.getSlug().isEmpty()) {
     * post.setSlug(generateSlug(post.getTitle()));
     * }
     * 
     * // Process sections
     * List<PostSection> sections = new ArrayList<>();
     * for (int i = 0; i < sectionTitles.size(); i++) {
     * PostSection section = new PostSection();
     * section.setSectionTitle(sectionTitles.get(i));
     * section.setContent(contents.get(i));
     * section.setPosition(positions.get(i));
     * 
     * // Process images for this section
     * List<SectionImage> images = new ArrayList<>();
     * // Logic to handle images would go here
     * 
     * // Process videos for this section
     * List<SectionVideo> videos = new ArrayList<>();
     * // Logic to handle videos would go here
     * 
     * section.setImages(images);
     * section.setVideos(videos);
     * sections.add(section);
     * }
     * 
     * post.setSections(sections);
     * 
     * postService.savePost(post, principal.getName());
     * return "redirect:/posts/" + post.getSlug();
     * }
     * 
     */

}