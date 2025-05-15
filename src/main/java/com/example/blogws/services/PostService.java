package com.example.blogws.services;

import com.example.blogws.exception.ResourceNotFoundException;
import com.example.blogws.models.*;
import com.example.blogws.repositories.CategoryRepository;
import com.example.blogws.repositories.PostRepository;
import com.example.blogws.repositories.SectionImageRepository;
import com.example.blogws.repositories.SectionVideoRepository;
import com.example.blogws.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SectionImageRepository sectionImageRepository;
    private final SectionVideoRepository sectionVideoRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,
            CategoryRepository categoryRepository, SectionImageRepository sectionImageRepository,
            SectionVideoRepository sectionVideoRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.sectionImageRepository = sectionImageRepository;
        this.sectionVideoRepository = sectionVideoRepository;
    }

    @Transactional
    public void savePost(Post post, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setAuthor(author);

        // Set post reference for each section
        if (post.getSections() != null) {
            for (PostSection section : post.getSections()) {
                section.setPost(post);

                // Set section reference for each image and video
                if (section.getImages() != null) {
                    for (SectionImage image : section.getImages()) {
                        image.setSection(section);
                    }
                }

                if (section.getVideos() != null) {
                    for (SectionVideo video : section.getVideos()) {
                        video.setSection(section);
                    }
                }
            }
        }

        postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public Post getPostBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    // Get all published posts with pagination
    public Page<Post> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findByIsPublishedTrue(pageable);
    }

    // Get all posts by category with pagination
    public Page<Post> getPostsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return postRepository.findByCategoryAndIsPublishedTrue(category, pageable);
    }

    // Get all posts by current user (both published and drafts) with pagination
    public Page<Post> getPostsByCurrentUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return postRepository.findByAuthor(user, pageable);
    }

    // Get recent posts (for sidebar or featured section)
    public List<Post> getRecentPosts() {
        return postRepository.findTop5ByIsPublishedTrueOrderByCreatedAtDesc();
    }

    // Search posts by title
    public Page<Post> searchPostsByTitle(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseAndIsPublishedTrue(keyword, pageable);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

    //

    public Post updatePost(Long postId, Post updatedPost, String username,
            List<String> sectionTitles, List<String> contents, List<Integer> positions,
            List<MultipartFile> imageFiles, List<String> imageCaptions, List<Integer> imagePositions,
            List<String> videoUrls, List<String> videoCaptions, List<Integer> videoPositions,
            List<Long> existingSectionIds, List<Long> existingImageIds, List<Long> existingVideoIds,
            List<Long> deletedImageIds, List<Long> deletedVideoIds) {

        Post existingPost = postRepository.findByIdWithSections(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (!existingPost.getAuthor().equals(author)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        // Cập nhật thông tin cơ bản
        updateBasicPostInfo(existingPost, updatedPost);

        // Xử lý xóa ảnh/video trước
        handleDeletedMedia(deletedImageIds, deletedVideoIds);

        // Xử lý các section
        processPostSections(existingPost, sectionTitles, contents, positions,
                imageFiles, imageCaptions, imagePositions,
                videoUrls, videoCaptions, videoPositions,
                existingSectionIds, existingImageIds, existingVideoIds);

        // Cập nhật thumbnail
        updatePostThumbnail(existingPost);

        return postRepository.save(existingPost);
    }

    private void updateBasicPostInfo(Post existingPost, Post updatedPost) {
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setCategory(updatedPost.getCategory());
        existingPost.setUpdatedAt(LocalDateTime.now());
        existingPost.setPublished(updatedPost.isPublished());

        // Chỉ cập nhật slug nếu tiêu đề thay đổi
        if (!existingPost.getTitle().equals(updatedPost.getTitle())) {
            existingPost.setSlug(generateSlug(updatedPost.getTitle()));
        }
    }

    private void handleDeletedMedia(List<Long> deletedImageIds, List<Long> deletedVideoIds) {
        if (deletedImageIds != null) {
            for (Long imageId : deletedImageIds) {
                SectionImage image = sectionImageRepository.findById(imageId).orElse(null);
                if (image != null) {
                    // Xóa file vật lý
                    try {
                        Path imagePath = Paths.get("." + image.getImageUrl());
                        Files.deleteIfExists(imagePath);
                    } catch (IOException e) {
                        // Log lỗi nhưng không dừng xử lý
                        System.err.println("Lỗi khi xóa file ảnh: " + e.getMessage());
                    }
                    // Xóa từ database
                    sectionImageRepository.deleteById(imageId);
                }
            }
        }

        if (deletedVideoIds != null) {
            sectionVideoRepository.deleteAllById(deletedVideoIds);
        }
    }

    private void processPostSections(Post post,
            List<String> sectionTitles, List<String> contents, List<Integer> positions,
            List<MultipartFile> imageFiles, List<String> imageCaptions, List<Integer> imagePositions,
            List<String> videoUrls, List<String> videoCaptions, List<Integer> videoPositions,
            List<Long> existingSectionIds, List<Long> existingImageIds, List<Long> existingVideoIds) {

        // Chuẩn bị dữ liệu hiện có
        Map<Long, PostSection> existingSections = post.getSections().stream()
                .collect(Collectors.toMap(PostSection::getId, Function.identity()));

        // Xử lý từng section
        List<PostSection> updatedSections = new ArrayList<>();
        int imageCounter = 0;
        int videoCounter = 0;

        for (int i = 0; i < sectionTitles.size(); i++) {
            PostSection section = getOrCreateSection(
                    existingSectionIds, existingSections, i, post);

            // Cập nhật thông tin cơ bản của section
            updateSectionInfo(section, sectionTitles.get(i), contents.get(i), positions.get(i));

            // Xử lý ảnh cho section
            imageCounter = processSectionImages(
                    section, imageFiles, imageCaptions, imagePositions,
                    existingImageIds, imageCounter);

            // Xử lý video cho section
            videoCounter = processSectionVideos(
                    section, videoUrls, videoCaptions, videoPositions,
                    existingVideoIds, videoCounter);

            updatedSections.add(section);
        }

        post.setSections(updatedSections);
    }

    private PostSection getOrCreateSection(List<Long> existingSectionIds,
            Map<Long, PostSection> existingSections,
            int index, Post post) {
        if (existingSectionIds != null && index < existingSectionIds.size()) {
            Long sectionId = existingSectionIds.get(index);
            return existingSections.getOrDefault(sectionId, new PostSection());
        }
        return new PostSection();
    }

    private void updateSectionInfo(PostSection section, String title, String content, Integer position) {
        section.setSectionTitle(title);
        section.setContent(content);
        section.setPosition(position);
    }

    private int processSectionImages(PostSection section,
            List<MultipartFile> imageFiles, List<String> imageCaptions,
            List<Integer> imagePositions, List<Long> existingImageIds,
            int startIndex) {

        List<SectionImage> images = new ArrayList<>();
        int currentIndex = startIndex;

        // Xử lý ảnh hiện có
        if (existingImageIds != null) {
            for (SectionImage existingImage : section.getImages()) {
                if (existingImageIds.contains(existingImage.getId())) {
                    updateExistingImage(existingImage, imageCaptions, imagePositions, currentIndex);
                    images.add(existingImage);
                    currentIndex++;
                }
            }
        }

        // Xử lý ảnh mới
        if (imageFiles != null) {
            while (currentIndex < imageFiles.size()) {
                MultipartFile file = imageFiles.get(currentIndex);

                if (!file.isEmpty()) {
                    try {
                        SectionImage newImage = createNewImage(
                                file,
                                getValueOrEmpty(imageCaptions, currentIndex),
                                getValueOrDefault(imagePositions, currentIndex, currentIndex + 1),
                                section);
                        images.add(newImage);
                    } catch (IOException e) {
                        System.err.println("Lỗi khi lưu ảnh: " + e.getMessage());
                    }
                }

                currentIndex++;
                if (shouldBreakProcessing(imagePositions, currentIndex))
                    break;
            }
        }

        section.setImages(images);
        return currentIndex;
    }

    private int processSectionVideos(PostSection section,
            List<String> videoUrls, List<String> videoCaptions,
            List<Integer> videoPositions, List<Long> existingVideoIds,
            int startIndex) {

        List<SectionVideo> videos = new ArrayList<>();
        int currentIndex = startIndex;

        // Xử lý video hiện có
        if (existingVideoIds != null) {
            for (SectionVideo existingVideo : section.getVideos()) {
                if (existingVideoIds.contains(existingVideo.getId())) {
                    updateExistingVideo(existingVideo, videoUrls, videoCaptions, videoPositions, currentIndex);
                    videos.add(existingVideo);
                    currentIndex++;
                }
            }
        }

        // Xử lý video mới
        if (videoUrls != null) {
            while (currentIndex < videoUrls.size()) {
                String url = videoUrls.get(currentIndex);

                if (url != null && !url.trim().isEmpty()) {
                    SectionVideo newVideo = createNewVideo(
                            url,
                            getValueOrEmpty(videoCaptions, currentIndex),
                            getValueOrDefault(videoPositions, currentIndex, currentIndex + 1),
                            section);
                    videos.add(newVideo);
                }

                currentIndex++;
                if (shouldBreakProcessing(videoPositions, currentIndex))
                    break;
            }
        }

        section.setVideos(videos);
        return currentIndex;
    }

    private void updateExistingImage(SectionImage image, List<String> captions,
            List<Integer> positions, int index) {
        if (index < captions.size())
            image.setCaption(captions.get(index));
        if (index < positions.size())
            image.setPosition(positions.get(index));
    }

    private SectionImage createNewImage(MultipartFile file, String caption,
            Integer position, PostSection section) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/images/");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        SectionImage newImage = new SectionImage();
        newImage.setImageUrl("/uploads/images/" + filename);
        newImage.setCaption(caption);
        newImage.setPosition(position);
        newImage.setSection(section);

        return newImage;
    }

    private void updateExistingVideo(SectionVideo video, List<String> urls,
            List<String> captions, List<Integer> positions, int index) {
        if (index < urls.size())
            video.setVideoUrl(urls.get(index));
        if (index < captions.size())
            video.setCaption(captions.get(index));
        if (index < positions.size())
            video.setPosition(positions.get(index));
    }

    private SectionVideo createNewVideo(String url, String caption,
            Integer position, PostSection section) {
        SectionVideo newVideo = new SectionVideo();
        newVideo.setVideoUrl(url);
        newVideo.setCaption(caption);
        newVideo.setPosition(position);
        newVideo.setSection(section);
        return newVideo;
    }

    private String getValueOrEmpty(List<String> list, int index) {
        return index < list.size() ? list.get(index) : "";
    }

    private Integer getValueOrDefault(List<Integer> list, int index, Integer defaultValue) {
        return index < list.size() ? list.get(index) : defaultValue;
    }

    private boolean shouldBreakProcessing(List<Integer> positions, int currentIndex) {
        return currentIndex < positions.size() && positions.get(currentIndex) == 1;
    }

    private void updatePostThumbnail(Post post) {
        for (PostSection section : post.getSections()) {
            for (SectionImage image : section.getImages()) {
                post.setThumbnailUrl(image.getImageUrl());
                return;
            }
        }
        post.setThumbnailUrl(null);
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

}