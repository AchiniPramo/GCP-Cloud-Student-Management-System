package lk.ijse.eca.cloud_rdbms.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class GcsConfig {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.credentials-location:}")
    private String credentialsLocation;

    /**
     * Configure Google Cloud Storage client
     */
    @Bean
    public Storage storage() {
        try {
            GoogleCredentials credentials;

            if (credentialsLocation != null && !credentialsLocation.isBlank()) {
                credentials = loadCredentials(credentialsLocation);
                System.out.println("Initialized Google Cloud Storage using configured credentials location");
            } else {
                credentials = GoogleCredentials.getApplicationDefault();
                System.out.println("Initialized Google Cloud Storage using application default credentials");
            }

            Storage gcsStorage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build()
                    .getService();

            System.out.println("Google Cloud Storage initialized successfully");
            return gcsStorage;
        } catch (Exception e1) {
            try {
                System.out.println("Falling back to application default credentials...");
                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

                Storage gcsStorage = StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build()
                        .getService();

                System.out.println("Google Cloud Storage initialized with default credentials");
                return gcsStorage;
            } catch (Exception e2) {
                System.err.println("GCS configuration error: Could not initialize Storage client");
                System.err.println("- Check gcp.credentials-location path or GOOGLE_APPLICATION_CREDENTIALS");
                System.err.println("- Image upload feature will be skipped");
                return null;
            }
        }
    }

    private GoogleCredentials loadCredentials(String location) throws Exception {
        String normalized = location.trim();

        // Support '/file.json' and 'file.json' as classpath resources.
        String classpathLocation = normalized.startsWith("/") ? normalized.substring(1) : normalized;
        ClassPathResource classPathResource = new ClassPathResource(classpathLocation);
        if (classPathResource.exists()) {
            try (InputStream inputStream = classPathResource.getInputStream()) {
                return GoogleCredentials.fromStream(inputStream);
            }
        }

        // Support absolute/relative filesystem paths.
        Path filePath = Path.of(normalized);
        if (Files.exists(filePath)) {
            try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile())) {
                return GoogleCredentials.fromStream(fileInputStream);
            }
        }

        throw new IllegalArgumentException("Credentials file not found at: " + location);
    }
}
