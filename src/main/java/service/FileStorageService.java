package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${aws.s3.bucket:}")
    private String s3BucketName;
    
    @Autowired
    private Environment environment;
    
    @Autowired(required = false)
    private S3Client s3Client;

    /**
     * Store file either locally or in S3 depending on the active profile
     */
    public String storeFile(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        
        if (environment.acceptsProfiles(Profiles.of("prod")) && s3Client != null && !s3BucketName.isEmpty()) {
            // In production with S3 bucket configured
            return storeFileInS3(file, fileName);
        } else {
            // In development or without S3 configured, use local storage
            return storeFileLocally(file, fileName);
        }
    }
    
    /**
     * Store file in local filesystem
     */
    private String storeFileLocally(MultipartFile file, String fileName) throws IOException {
        // Create the directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Copy the file to the target location
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        return fileName;
    }
    
    /**
     * Store file in S3
     */
    private String storeFileInS3(MultipartFile file, String fileName) throws IOException {
        try {
            // Upload to S3
            s3Client.putObject(builder -> builder
                    .bucket(s3BucketName)
                    .key(fileName)
                    .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            return fileName;
        } catch (Exception e) {
            // Fallback to local storage if S3 upload fails
            System.err.println("S3 upload failed: " + e.getMessage());
            return storeFileLocally(file, fileName);
        }
    }
    
    /**
     * Get file URL - handles both local and S3 storage
     */
    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        if (environment.acceptsProfiles(Profiles.of("prod")) && s3Client != null && !s3BucketName.isEmpty()) {
            try {
                // Get S3 URL
                GetUrlRequest request = GetUrlRequest.builder()
                        .bucket(s3BucketName)
                        .key(fileName)
                        .build();
                URL url = s3Client.utilities().getUrl(request);
                return url.toString();
            } catch (Exception e) {
                System.err.println("Failed to get S3 URL: " + e.getMessage());
                // Fallback to local URL format
                return "/uploads/" + fileName;
            }
        } else {
            // Local file URL
            return "/uploads/" + fileName;
        }
    }
    
    /**
     * Generate a unique filename to prevent collisions
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Get file path - for locally stored files
     */
    public Path getFilePath(String fileName) {
        return Paths.get(uploadDir).resolve(fileName);
    }
    
    public void deleteFile(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
            Files.deleteIfExists(filePath);
            System.out.println("File deleted successfully: " + filePath.toString());
        } catch (IOException ex) {
            System.err.println("Failed to delete file: " + ex.getMessage());
            throw new IOException("Failed to delete file: " + ex.getMessage(), ex);
        }
    }
} 