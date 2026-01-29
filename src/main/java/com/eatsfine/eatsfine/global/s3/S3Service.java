package com.eatsfine.eatsfine.global.s3;

import com.eatsfine.eatsfine.domain.image.exception.ImageException;
import com.eatsfine.eatsfine.domain.image.status.ImageErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-url}")
    private String baseUrl;

    public String upload(MultipartFile file, String directory) {
        String key = generateKey(file, directory);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return key;
        } catch (IOException e) {
            throw new ImageException(ImageErrorStatus.S3_UPLOAD_FAILED);
        }
    }

    public void deleteByKey(String key) {
        if (key == null || key.isBlank()) {
            throw new ImageException(ImageErrorStatus._INVALID_IMAGE_KEY);
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public String toUrl(String key) {
        if (key == null || key.isBlank()) return null;
        return baseUrl + "/" + key;
    }

    private String generateKey(MultipartFile file, String directory) {
        if(directory == null || directory.isBlank()) {
            throw new ImageException(ImageErrorStatus._INVALID_S3_DIRECTORY);
        }
        String extension = extractExtension(file.getOriginalFilename());
        return directory + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new ImageException(ImageErrorStatus.INVALID_FILE_TYPE);
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}