package com.example.lms.service;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import com.example.lms.dto.S3FileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    public S3Service(S3Client s3Client, S3Presigner presigner) {
        this.s3Client = s3Client;
        this.presigner = presigner;
    }

    public String uploadSyllabus(MultipartFile file) {

        try {
            String key =
                    "syllabus/" +
                            UUID.randomUUID() + "-" +
                            file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            return "https://" + bucketName + ".s3.amazonaws.com/" + key;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload syllabus to S3", e);
        }
    }
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String key = folder + "/" +
                    UUID.randomUUID() + "-" +
                    file.getOriginalFilename();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            return "https://" + bucketName + ".s3.amazonaws.com/" + key;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
    private String extractKeyFromUrl(String fileUrl) {

        // Example URL:
        // https://bucket-name.s3.amazonaws.com/course-content/uuid-file.pdf

        int index = fileUrl.indexOf(".amazonaws.com/");
        if (index == -1) {
            throw new IllegalArgumentException("Invalid S3 file URL");
        }

        return fileUrl.substring(index + ".amazonaws.com/".length());
    }


    public S3FileResponse downloadFile(String fileUrl) {

        try {
            String key = extractKeyFromUrl(fileUrl);

            ResponseInputStream<GetObjectResponse> s3Object =
                    s3Client.getObject(
                            GetObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(key)
                                    .build()
                    );

            byte[] bytes = s3Object.readAllBytes();

            String contentType =
                    s3Object.response().contentType();

            String fileName =
                    key.substring(key.lastIndexOf("/") + 1);

            return new S3FileResponse(bytes, contentType, fileName);

        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }
    public String generatePreviewUrl(String fileUrl) {

        String key = extractKeyFromUrl(fileUrl);

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                presigner.presignGetObject(
                        GetObjectPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(10))
                                .getObjectRequest(getObjectRequest)
                                .build()
                );

        return presignedRequest.url().toString();
    }

}
