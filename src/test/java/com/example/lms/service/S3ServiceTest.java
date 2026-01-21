package com.example.lms.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setup() {
        // Inject @Value field
        ReflectionTestUtils.setField(
                s3Service,
                "bucketName",
                "test-bucket"
        );
    }

    /* ================= SUCCESS ================= */

    @Test
    void uploadSyllabus_success() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "syllabus",
                        "test.pdf",
                        "application/pdf",
                        "dummy-content".getBytes()
                );

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenReturn(PutObjectResponse.builder().build());

        String url = s3Service.uploadSyllabus(file);

        assertNotNull(url);
        assert(url.contains("test-bucket.s3.amazonaws.com/syllabus/"));

        verify(s3Client).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    /* ================= FAILURE ================= */

    @Test
    void uploadSyllabus_fail_io_exception() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "syllabus",
                        "test.pdf",
                        "application/pdf",
                        new byte[0]
                ) {
                    @Override
                    public byte[] getBytes() throws IOException {
                        throw new IOException("IO error");
                    }
                };

        assertThrows(
                RuntimeException.class,
                () -> s3Service.uploadSyllabus(file)
        );
    }
}
