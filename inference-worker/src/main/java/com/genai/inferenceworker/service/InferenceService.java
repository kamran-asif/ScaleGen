package com.genai.inferenceworker.service;

import com.genai.common.dto.InferenceRequest;
import com.genai.common.dto.InferenceResponse;
import com.genai.common.dto.TokenUsage;
import com.genai.common.util.KafkaConstants;
import com.genai.inferenceworker.client.MultiLLMClientManager;
import com.genai.inferenceworker.resilience.CircuitBreaker;
import com.genai.inferenceworker.router.IntelligentRouter;
import com.genai.inferenceworker.router.ModelSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InferenceService {

    private static final Logger log = LoggerFactory.getLogger(InferenceService.class);

    private final IntelligentRouter intelligentRouter;
    private final CircuitBreaker circuitBreaker;
    private final MultiLLMClientManager llmClientManager;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InferenceService(IntelligentRouter intelligentRouter,
                            CircuitBreaker circuitBreaker,
                            MultiLLMClientManager llmClientManager,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.intelligentRouter = intelligentRouter;
        this.circuitBreaker = circuitBreaker;
        this.llmClientManager = llmClientManager;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaConstants.INFERENCE_WORKER_TOPIC, groupId = "inference-worker-group")
    public void processInference(InferenceRequest request, Acknowledgment ack) {
        log.info("[InferenceWorker] Received request {} with strategy: {}", request.getRequestId(), request.getRoutingStrategy());
        Instant startTime = Instant.now();

        List<ModelSpec> candidates = intelligentRouter.selectRoute(
                request.getModel(), request.getRoutingStrategy(), request.getPrompt());

        List<String> attemptedModels = new ArrayList<>();
        String successfulResponseText = null;
        ModelSpec selectedModel = null;
        String circuitState = "CLOSED";

        for (ModelSpec candidate : candidates) {
            attemptedModels.add(candidate.getName());

            if (!circuitBreaker.allowExecution(candidate.getId())) {
                log.warn("[Resilience] Circuit open for {}. Tripping to next fallback model.", candidate.getId());
                continue;
            }

            try {
                log.info("[InferenceWorker] Invoking model {} for request {}", candidate.getId(), request.getRequestId());
                successfulResponseText = llmClientManager.executeInference(candidate, request.getPrompt());
                selectedModel = candidate;
                circuitBreaker.recordSuccess(candidate.getId());
                circuitState = circuitBreaker.getState(candidate.getId());
                break;
            } catch (Exception e) {
                log.error("[Resilience] Execution failed on model {}: {}", candidate.getId(), e.getMessage());
                circuitBreaker.recordFailure(candidate.getId());
            }
        }

        int processingTime = (int) Duration.between(startTime, Instant.now()).toMillis();

        if (successfulResponseText != null && selectedModel != null) {
            TokenUsage tokens = TokenUsage.calculate(request.getPrompt(), successfulResponseText,
                    selectedModel.getCostPer1kInputUsd(), selectedModel.getCostPer1kOutputUsd());

            InferenceResponse response = InferenceResponse.builder()
                    .requestId(request.getRequestId())
                    .response(successfulResponseText)
                    .model(request.getModel())
                    .selectedModel(selectedModel.getId())
                    .processedAt(LocalDateTime.now())
                    .success(true)
                    .processingTimeMs(processingTime)
                    .tokenUsage(tokens)
                    .fallbackChain(attemptedModels)
                    .circuitBreakerState(circuitState)
                    .traceId(request.getTraceId())
                    .spanId(request.getSpanId())
                    .build();

            log.info("[InferenceWorker] Success! Request {} executed on {} in {}ms (Cost: ${})",
                    request.getRequestId(), selectedModel.getId(), processingTime, tokens.getEstimatedCostUsd());
            kafkaTemplate.send(KafkaConstants.RESPONSE_PROCESSING_TOPIC, request.getRequestId(), response);
        } else {
            handleFailure(request, attemptedModels, processingTime);
        }

        ack.acknowledge();
    }

    private void handleFailure(InferenceRequest request, List<String> attemptedModels, int processingTime) {
        log.error("[Resilience] All model candidates failed for request: {}", request.getRequestId());
        
        InferenceResponse failedResponse = InferenceResponse.builder()
                .requestId(request.getRequestId())
                .model(request.getModel())
                .processedAt(LocalDateTime.now())
                .success(false)
                .errorMessage("All model candidates in fallback chain failed: " + String.join(" -> ", attemptedModels))
                .processingTimeMs(processingTime)
                .fallbackChain(attemptedModels)
                .traceId(request.getTraceId())
                .spanId(request.getSpanId())
                .build();

        kafkaTemplate.send(KafkaConstants.DLQ_TOPIC, request.getRequestId(), request);
        kafkaTemplate.send(KafkaConstants.INFERENCE_RESPONSE_TOPIC, request.getRequestId(), failedResponse);
    }
}
