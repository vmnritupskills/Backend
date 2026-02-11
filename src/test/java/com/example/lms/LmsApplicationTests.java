package com.example.lms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtKeyConfig.class)
class LmsApplicationTests {

    @MockBean
    private S3Client s3Client;

    @MockBean
    private S3Presigner s3Presigner;

    @Test
    void contextLoads() {
    }
}
