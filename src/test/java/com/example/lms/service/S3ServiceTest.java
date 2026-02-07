package com.example.lms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.example.lms.dto.S3FileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3Service Tests")
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner presigner;

    @InjectMocks
    private S3Service s3Service;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String IMAGE_CONTENT_TYPE = "image/jpeg";

    @BeforeEach
    void setup() {
        // Inject @Value field
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET_NAME);
    }

    @Nested
    @DisplayName("uploadSyllabus - Happy Path Tests")
    class UploadSyllabusHappyPath {

        @Test
        @DisplayName("Should successfully upload syllabus PDF file")
        void uploadSyllabus_success() {
            // Arrange
            byte[] fileContent = "syllabus content".getBytes();
            MockMultipartFile file = new MockMultipartFile(
                    "syllabus",
                    "course-syllabus.pdf",
                    PDF_CONTENT_TYPE,
                    fileContent
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Act
            String url = s3Service.uploadSyllabus(file);

            // Assert
            assertNotNull(url);
            assertTrue(url.contains(BUCKET_NAME + ".s3.amazonaws.com/syllabus/"));
            assertTrue(url.contains("course-syllabus.pdf"));
            assertTrue(url.startsWith("https://"));

            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should generate unique key for each syllabus upload")
        void uploadSyllabus_generatesUniqueKeys() {
            // Arrange
            MockMultipartFile file1 = new MockMultipartFile(
                    "syllabus", "test.pdf", PDF_CONTENT_TYPE, "content1".getBytes()
            );
            MockMultipartFile file2 = new MockMultipartFile(
                    "syllabus", "test.pdf", PDF_CONTENT_TYPE, "content2".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Act
            String url1 = s3Service.uploadSyllabus(file1);
            String url2 = s3Service.uploadSyllabus(file2);

            // Assert
            assertNotEquals(url1, url2);
            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should preserve original filename in URL")
        void uploadSyllabus_preservesFilename() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "syllabus", "advanced-java.pdf", PDF_CONTENT_TYPE, "content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Act
            String url = s3Service.uploadSyllabus(file);

            // Assert
            assertTrue(url.contains("advanced-java.pdf"));
        }
    }

    @Nested
    @DisplayName("uploadSyllabus - Unhappy Path Tests")
    class UploadSyllabusUnhappyPath {

        @Test
        @DisplayName("Should throw RuntimeException when file read fails with IOException")
        void uploadSyllabus_ioException_throwsRuntimeException() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "syllabus", "test.pdf", PDF_CONTENT_TYPE, new byte[0]
            ) {
                @Override
                public byte[] getBytes() throws IOException {
                    throw new IOException("IO error while reading file");
                }
            };

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> s3Service.uploadSyllabus(file)
            );

            assertTrue(exception.getMessage().contains("Failed to upload syllabus to S3"));
            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should handle empty file upload")
        void uploadSyllabus_emptyFile_success() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "syllabus", "empty.pdf", PDF_CONTENT_TYPE, new byte[0]
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Act
            String url = s3Service.uploadSyllabus(file);

            // Assert
            assertNotNull(url);
            assertTrue(url.contains("empty.pdf"));
        }
    }

    @Nested
    @DisplayName("uploadFile - Happy Path Tests")
    class UploadFileHappyPath {

        @Test
        @DisplayName("Should successfully upload file with custom folder")
        void uploadFile_success() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file", "course-content.pdf", PDF_CONTENT_TYPE, "content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Act
            String url = s3Service.uploadFile(file, "course-materials");

            // Assert
            assertNotNull(url);
            assertTrue(url.contains(BUCKET_NAME + ".s3.amazonaws.com/course-materials/"));
            assertTrue(url.contains("course-content.pdf"));
            assertTrue(url.startsWith("https://"));
        }

        @Test
        @DisplayName("Should upload to different folder paths")
        void uploadFile_variousFolders() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file", "document.pdf", PDF_CONTENT_TYPE, "content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Act
            String urlAssignments = s3Service.uploadFile(file, "assignments");
            String urlResources = s3Service.uploadFile(file, "resources");

            // Assert
            assertTrue(urlAssignments.contains("assignments/"));
            assertTrue(urlResources.contains("resources/"));
            assertNotEquals(urlAssignments, urlResources);
        }

        @Test
        @DisplayName("Should handle image file upload")
        void uploadFile_imageFile_success() {
            // Arrange
            MockMultipartFile imageFile = new MockMultipartFile(
                    "file", "course-banner.jpg", IMAGE_CONTENT_TYPE, "image-data".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Act
            String url = s3Service.uploadFile(imageFile, "images");

            // Assert
            assertNotNull(url);
            assertTrue(url.contains("course-banner.jpg"));
        }
    }

    @Nested
    @DisplayName("uploadFile - Unhappy Path Tests")
    class UploadFileUnhappyPath {

        @Test
        @DisplayName("Should throw RuntimeException when file upload fails")
        void uploadFile_ioException_throwsRuntimeException() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", PDF_CONTENT_TYPE, new byte[0]
            ) {
                @Override
                public byte[] getBytes() throws IOException {
                    throw new IOException("Read failed");
                }
            };

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> s3Service.uploadFile(file, "test-folder")
            );

            assertTrue(exception.getMessage().contains("Failed to upload file to S3"));
        }
    }

    @Nested
    @DisplayName("downloadFile - Happy Path Tests")
    class DownloadFileHappyPath {

        @Test
        @DisplayName("Should successfully download file from S3")
        void downloadFile_success() throws IOException {
            // Arrange
            String fileUrl = "https://test-bucket.s3.amazonaws.com/course-content/uuid-document.pdf";
            byte[] fileContent = "file content here".getBytes();

            ResponseInputStream<GetObjectResponse> responseInputStream = mock(ResponseInputStream.class);
            GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

            when(responseInputStream.readAllBytes()).thenReturn(fileContent);
            when(responseInputStream.response()).thenReturn(getObjectResponse);
            when(getObjectResponse.contentType()).thenReturn(PDF_CONTENT_TYPE);
            when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

            // Act
            S3FileResponse response = s3Service.downloadFile(fileUrl);

            // Assert
            assertNotNull(response);
            assertArrayEquals(fileContent, response.data());
            assertEquals(PDF_CONTENT_TYPE, response.contentType());
            assertEquals("uuid-document.pdf", response.fileName());
        }

        @Test
        @DisplayName("Should extract correct filename from URL during download")
        void downloadFile_extractsCorrectFilename() throws IOException {
            // Arrange
            String fileUrl = "https://test-bucket.s3.amazonaws.com/assignments/uuid-homework.pdf";
            byte[] fileContent = "homework".getBytes();

            ResponseInputStream<GetObjectResponse> responseInputStream = mock(ResponseInputStream.class);
            GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

            when(responseInputStream.readAllBytes()).thenReturn(fileContent);
            when(responseInputStream.response()).thenReturn(getObjectResponse);
            when(getObjectResponse.contentType()).thenReturn(PDF_CONTENT_TYPE);
            when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

            // Act
            S3FileResponse response = s3Service.downloadFile(fileUrl);

            // Assert
            assertEquals("uuid-homework.pdf", response.fileName());
        }

        @Test
        @DisplayName("Should handle large file downloads")
        void downloadFile_largeFile_success() throws IOException {
            // Arrange
            String fileUrl = "https://test-bucket.s3.amazonaws.com/videos/uuid-course-video.mp4";
            byte[] largeFileContent = new byte[10 * 1024 * 1024]; // 10MB
            for (int i = 0; i < largeFileContent.length; i++) {
                largeFileContent[i] = (byte) (i % 256);
            }

            ResponseInputStream<GetObjectResponse> responseInputStream = mock(ResponseInputStream.class);
            GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

            when(responseInputStream.readAllBytes()).thenReturn(largeFileContent);
            when(responseInputStream.response()).thenReturn(getObjectResponse);
            when(getObjectResponse.contentType()).thenReturn("video/mp4");
            when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

            // Act
            S3FileResponse response = s3Service.downloadFile(fileUrl);

            // Assert
            assertNotNull(response);
            assertEquals(largeFileContent.length, response.data().length);
        }
    }

    @Nested
    @DisplayName("downloadFile - Unhappy Path Tests")
    class DownloadFileUnhappyPath {

        @Test
        @DisplayName("Should throw IOException when download fails")
        void downloadFile_ioException_throwsRuntimeException() throws IOException {
            // Arrange
            String fileUrl = "https://test-bucket.s3.amazonaws.com/course-content/uuid-document.pdf";

            ResponseInputStream<GetObjectResponse> responseInputStream = mock(ResponseInputStream.class);
            when(responseInputStream.readAllBytes()).thenThrow(new IOException("Download failed"));
            when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> s3Service.downloadFile(fileUrl)
            );

            assertTrue(exception.getMessage().contains("Failed to download file from S3"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid URL format")
        void downloadFile_invalidUrl_throwsException() {
            // Arrange
            String invalidUrl = "https://invalid-url/no-amazonaws-domain.pdf";

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> s3Service.downloadFile(invalidUrl)
            );

            assertEquals("Invalid S3 file URL", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for malformed S3 URL")
        void downloadFile_malformedUrl_throwsException() {
            // Arrange
            String malformedUrl = "https://test-bucket.s3.com/file.pdf"; // missing amazonaws.com

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> s3Service.downloadFile(malformedUrl)
            );
        }
    }

    @Nested
    @DisplayName("generatePreviewUrl - Happy Path Tests")
    class GeneratePreviewUrlHappyPath {

        @Test
        @DisplayName("Should successfully generate presigned preview URL")
        void generatePreviewUrl_success() {
            // Arrange
            String fileUrl = "https://test-bucket.s3.amazonaws.com/course-content/uuid-document.pdf";
            String presignedUrl = "https://test-bucket.s3.amazonaws.com/course-content/uuid-document.pdf?X-Amz-SignedHeaders=...";

            PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
            java.net.URL mockUrl = mock(java.net.URL.class);

            when(mockUrl.toString()).thenReturn(presignedUrl);
            when(presignedRequest.url()).thenReturn(mockUrl);
            doReturn(presignedRequest).when(presigner).presignGetObject(any(GetObjectPresignRequest.class));

            // Act
            String result = s3Service.generatePreviewUrl(fileUrl);

            // Assert
            assertNotNull(result);
            assertEquals(presignedUrl, result);
            verify(presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
        }

        @Test
        @DisplayName("Should generate different preview URLs for different files")
        void generatePreviewUrl_differentFiles_differentUrls() {
            // Arrange
            String fileUrl1 = "https://test-bucket.s3.amazonaws.com/file1.pdf";
            String fileUrl2 = "https://test-bucket.s3.amazonaws.com/file2.pdf";

            PresignedGetObjectRequest presignedRequest1 = mock(PresignedGetObjectRequest.class);
            PresignedGetObjectRequest presignedRequest2 = mock(PresignedGetObjectRequest.class);

            java.net.URL url1 = mock(java.net.URL.class);
            java.net.URL url2 = mock(java.net.URL.class);

            when(url1.toString()).thenReturn("https://presigned-url-1");
            when(url2.toString()).thenReturn("https://presigned-url-2");

            when(presignedRequest1.url()).thenReturn(url1);
            when(presignedRequest2.url()).thenReturn(url2);

            doReturn(presignedRequest1, presignedRequest2).when(presigner).presignGetObject(any(GetObjectPresignRequest.class));

            // Act
            String result1 = s3Service.generatePreviewUrl(fileUrl1);
            String result2 = s3Service.generatePreviewUrl(fileUrl2);

            // Assert
            assertNotEquals(result1, result2);
        }
    }

    @Nested
    @DisplayName("generatePreviewUrl - Unhappy Path Tests")
    class GeneratePreviewUrlUnhappyPath {

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid preview URL")
        void generatePreviewUrl_invalidUrl_throwsException() {
            // Arrange
            String invalidUrl = "https://invalid-domain.com/file.pdf";

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> s3Service.generatePreviewUrl(invalidUrl)
            );
        }

        @Test
        @DisplayName("Should handle URLs with special characters")
        void generatePreviewUrl_urlWithSpecialCharacters_success() {
            // Arrange
            String fileUrl = "https://test-bucket.s3.amazonaws.com/course%20materials/uuid-file%20name.pdf";

            PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
            java.net.URL mockUrl = mock(java.net.URL.class);

            when(mockUrl.toString()).thenReturn("https://presigned-url");
            when(presignedRequest.url()).thenReturn(mockUrl);
            doReturn(presignedRequest).when(presigner).presignGetObject(any(GetObjectPresignRequest.class));

            // Act
            String result = s3Service.generatePreviewUrl(fileUrl);

            // Assert
            assertNotNull(result);
            assertEquals("https://presigned-url", result);
        }
    }
}
