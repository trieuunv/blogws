$(document).ready(function () {
    let sectionCount = 1;

    // Add new section
    $('#add-section').click(function () {
        sectionCount++;
        const newSection = $(`<div class="section mb-4 p-3 border" id="section-${sectionCount - 1}">
                <h3>Section ${sectionCount}</h3>
                <div class="mb-3">
                    <label class="form-label">Section Title</label>
                    <input type="text" class="form-control" name="sectionTitles" required>
                </div>
                
                <div class="mb-3">
                    <label class="form-label">Content</label>
                    <textarea class="form-control" name="contents" rows="5" required></textarea>
                </div>
                
                <div class="mb-3">
                    <label class="form-label">Position</label>
                    <input type="number" class="form-control" name="positions" value="${sectionCount}" required>
                </div>
                
                <div class="mb-3 images-container">
                    <label class="form-label">Images</label>
                    <button type="button" class="btn btn-sm btn-outline-primary add-image">Add Image</button>
                </div>
                
                <div class="mb-3 videos-container">
                    <label class="form-label">Videos</label>
                    <button type="button" class="btn btn-sm btn-outline-primary add-video">Add Video</button>
                </div>
                
                <button type="button" class="btn btn-sm btn-outline-danger remove-section">Remove Section</button>
            </div>`);

        $('#sections-container').append(newSection);
    });

    // Add image to section
    $(document).on('click', '.add-image', function () {
        const container = $(this).closest('.images-container');
        const imageCount = container.find('.image-field').length;

        const newImage = $(`<div class="image-field mb-2 p-2 border">
                <div class="mb-2">
                    <label class="form-label">Image File</label>
                    <input type="file" class="form-control" name="imageFiles">
                </div>
                <div class="mb-2">
                    <label class="form-label">Caption</label>
                    <input type="text" class="form-control" name="imageCaptions">
                </div>
                <div class="mb-2">
                    <label class="form-label">Position</label>
                    <input type="number" class="form-control" name="imagePositions" value="${imageCount + 1}">
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger remove-image">Remove</button>
            </div>`);

        container.append(newImage);
    });

    // Add video to section
    $(document).on('click', '.add-video', function () {
        const container = $(this).closest('.videos-container');
        const videoCount = container.find('.video-field').length;

        const newVideo = $(`<div class="video-field mb-2 p-2 border">
                <div class="mb-2">
                    <label class="form-label">Video URL</label>
                    <input type="text" class="form-control" name="videoUrls">
                </div>
                <div class="mb-2">
                    <label class="form-label">Caption</label>
                    <input type="text" class="form-control" name="videoCaptions">
                </div>
                <div class="mb-2">
                    <label class="form-label">Position</label>
                    <input type="number" class="form-control" name="videoPositions" value="${videoCount + 1}">
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger remove-video">Remove</button>
            </div>`);

        container.append(newVideo);
    });

    // Remove elements
    $(document).on('click', '.remove-section', function () {
        $(this).closest('.section').remove();
        sectionCount--;
    });

    $(document).on('click', '.remove-image', function () {
        $(this).closest('.image-field').remove();
    });

    $(document).on('click', '.remove-video', function () {
        $(this).closest('.video-field').remove();
    });
});