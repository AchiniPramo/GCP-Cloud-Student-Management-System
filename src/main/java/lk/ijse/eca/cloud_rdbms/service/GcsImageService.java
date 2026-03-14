package lk.ijse.eca.cloud_rdbms.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GcsImageService {

    private static final String BUCKET_NAME = "silent-bird-489817-g0-bucket";
    private static final String GCS_BASE_URL = "https://storage.googleapis.com/";

    @Autowired(required = false)
    private Storage storage;

    /**
     * Upload image to Google Cloud Storage
     * @param file MultipartFile containing the image
     * @return Public URL of the uploaded image
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (storage == null) {
            throw new IOException("Google Cloud Storage is not configured. Please set GOOGLE_APPLICATION_CREDENTIALS env variable.");
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Create blob with public read access
        BlobId blobId = BlobId.of(BUCKET_NAME, filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .setMetadata(java.util.Collections.singletonMap("Cache-Control", "public, max-age=3600"))
                .build();

        // Upload file
        storage.create(blobInfo, file.getInputStream());

        // Return public URL
        return GCS_BASE_URL + BUCKET_NAME + "/" + filename;
    }

    /**
     * Delete image from Google Cloud Storage
     * @param imageUrl The public URL of the image
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || storage == null) {
            return;
        }

        try {
            // Extract filename from URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            BlobId blobId = BlobId.of(BUCKET_NAME, filename);
            storage.delete(blobId);
        } catch (Exception e) {
            System.err.println("Error deleting image: " + e.getMessage());
        }
    }
}