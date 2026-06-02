package com.genai.inferenceworker.service;

import com.genai.common.dto.InferenceRequest;
import com.genai.common.dto.InferenceResponse;
import com.genai.common.util.KafkaConstants;
import com.genai.inferenceworker.client.OpenAIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InferenceService {

    private final OpenAIClient openAIClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaConstants.INFERENCE_REQUEST_TOPIC + ".worker", groupId = "inference-worker-group")
    public void processInference(InferenceRequest request, Acknowledgment ack) {
        log.info("Processing inference request: {}", request.getRequestId());
        Instant startTime = Instant.now();

        try {
            String response = openAIClient.generateResponse(request.getPrompt(), request.getModel());
            int processingTime = (int) Duration.between(startTime, Instant.now()).toMillis();

            InferenceResponse inferenceResponse = InferenceResponse.builder()
                    .requestId(request.getRequestId())
                    .response(response)
                    .model(request.getModel())
                    .processedAt(LocalDateTime.now())
                    .success(true)
                    .processingTimeMs(processingTime)
                    .build();

            kafkaTemplate.send(KafkaConstants.RESPONSE_PROCESSING_TOPIC, request.getRequestId(), inferenceResponse);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing request {}: {}", request.getRequestId(), e.getMessage());
            handleFailure(request, e, startTime, ack);
        }
    }

    private void handleFailure(InferenceRequest request, Exception e, Instant startTime, Acknowledgment ack) {
        request.setRetryCount(request.getRetryCount() + 1);

        if (request.getRetryCount() <= request.getMaxRetries()) {
            log.warn("Retrying request {} (attempt {}/{})", request.getRequestId(), request.getRetryCount(), request.getMaxRetries());
            kafkaTemplate.send(KafkaConstants.RETRY_TOPIC, request.getRequestId(), request);
        } else {
            log.error("Request {} failed after {} retries", request.getRequestId(), request.getMaxRetries());
            int processingTime = (int) Duration.between(startTime, Instant.now()).toMillis();

            InferenceResponse failedResponse = InferenceResponse.builder()
                    .requestId(request.getRequestId())
                    .model(request.getModel())
                    .processedAt(LocalDateTime.now())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .processingTimeMs(processingTime)
                    .build();

            kafkaTemplate.send(KafkaConstants.DLQ_TOPIC, request.getRequestId(), request);
            kafkaTemplate.send(KafkaConstants.INFERENCE_RESPONSE_TOPIC, request.getRequestId(), failedResponse);
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = KafkaConstants.RETRY_TOPIC, groupId = "inference-worker-group")
    public void processRetry(InferenceRequest request, Acknowledgment ack) {
        log.info("Processing retry for request: {}", request.getRequestId());
        processInference(request, ack);
    }
}
