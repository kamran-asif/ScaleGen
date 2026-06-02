package com.genai.responseprocessor.service;

import com.genai.common.dto.InferenceResponse;
import com.genai.common.util.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseProcessingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaConstants.RESPONSE_PROCESSING_TOPIC, groupId = "response-processor-group")
    public void processResponse(InferenceResponse response) {
        log.info("Processing response for request: {}", response.getRequestId());

        String cleanedResponse = cleanResponse(response.getResponse());
        response.setResponse(cleanedResponse);

        kafkaTemplate.send(KafkaConstants.INFERENCE_RESPONSE_TOPIC, response.getRequestId(), response);
    }

    private String cleanResponse(String response) {
        if (response == null) {
            return "";
        }

        String cleaned = response.trim();
        
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.replaceAll("[^\\p{Print}\\n]", "");
        
        return cleaned;
    }
}
