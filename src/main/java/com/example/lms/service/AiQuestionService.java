package com.example.lms.service;

import com.example.lms.dto.AiQuestionGenerationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiQuestionService {

    private final RestTemplate restTemplate;

    @Value("${ai.question-service.base-url}")
    private String baseUrl;

    public AiQuestionGenerationResponseDTO generateFromChunk(
            String chunk,
            List<String> questionTypes,
            Integer numQuestions,
            Map<String, Double> difficultyDistribution
    ) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("chunk", chunk);
            body.add("question_types", String.join(",", questionTypes));
            body.add("num_questions", String.valueOf(numQuestions));

            if (difficultyDistribution != null && !difficultyDistribution.isEmpty()) {
                String difficultyString = difficultyDistribution.entrySet()
                        .stream()
                        .map(e -> e.getKey() + ":" + e.getValue())
                        .collect(Collectors.joining(","));
                body.add("difficulty", difficultyString);
            }

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            log.info("Calling AI service: /generate-from-chunk");

            ResponseEntity<AiQuestionGenerationResponseDTO> response =
                    restTemplate.exchange(
                            baseUrl + "/generate-from-chunk",
                            HttpMethod.POST,
                            request,
                            AiQuestionGenerationResponseDTO.class
                    );

            log.info("AI Response Status: {}", response.getStatusCode());
            log.debug("AI Response Body: {}", response.getBody());

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            log.error("AI returned error: {}", ex.getResponseBodyAsString());
            throw new RuntimeException("AI service error: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            log.error("AI call failed", ex);
            throw new RuntimeException("Failed to call AI service", ex);
        }
    }
}
