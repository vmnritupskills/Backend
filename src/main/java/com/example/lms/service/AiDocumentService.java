package com.example.lms.service;

import com.example.lms.dto.ProcessedContentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDocumentService {

    private final RestTemplate restTemplate;

    @Value("${ai.question-service.base-url}")
    private String aiBaseUrl;

    public ProcessedContentDTO uploadDocument(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            log.info("Uploading document to AI service");

            ResponseEntity<ProcessedContentDTO> response =
                    restTemplate.exchange(
                            aiBaseUrl + "/upload-document",
                            HttpMethod.POST,
                            request,
                            ProcessedContentDTO.class
                    );

            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded document", e);
        }
    }
}
