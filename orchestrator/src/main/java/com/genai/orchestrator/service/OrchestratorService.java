package com.genai.orchestrator.service;

import com.genai.common.dto.InferenceRequest;
import com.genai.common.dto.InferenceResponse;
import com.genai.common.dto.TokenUsage;
import com.genai.common.util.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IdempotencyService idempotencyService;

    public OrchestratorService(KafkaTemplate<String, Object> kafkaTemplate, IdempotencyService idempotencyService) {
        this.kafkaTemplate = kafkaTemplate;
        this.idempotencyService = idempotencyService;
    }

    @KafkaListener(topics = KafkaConstants.INFERENCE_REQUEST_TOPIC, groupId = "orchestrator-group")
    public void orchestrateRequest(InferenceRequest request) {
        log.info("[Orchestrator] Processing request: {}, User: {}, Strategy: {}",
                request.getRequestId(), request.getUserId(), request.getRoutingStrategy());

        // Step 1: Idempotency Validation
        String ik = request.getIdempotencyKey();
        if (ik == null || ik.isBlank()) {
            ik = idempotencyService.generateFingerprint(request.getUserId(), request.getPrompt(), request.getModel());
            request.setIdempotencyKey(ik);
        }

        Object cached = idempotencyService.getCachedResponse(ik);
        if (cached != null) {
            log.info("[Orchestrator] Idempotency Hit for key: {}. Instant return.", ik);
            InferenceResponse cachedResp = InferenceResponse.builder()
                    .requestId(request.getRequestId())
                    .response(cached.toString())
                    .model(request.getModel())
                    .selectedModel(request.getModel() + " (Cached)")
                    .processedAt(LocalDateTime.now())
                    .success(true)
                    .processingTimeMs(5)
                    .tokenUsage(TokenUsage.calculate(request.getPrompt(), cached.toString(), 0, 0))
                    .fallbackChain(List.of("Idempotent-Redis-Cache"))
                    .cached(true)
                    .traceId(request.getTraceId())
                    .spanId(request.getSpanId())
                    .build();
            kafkaTemplate.send(KafkaConstants.INFERENCE_RESPONSE_TOPIC, request.getRequestId(), cachedResp);
            return;
        }

        // Step 2: Context Enrichment & Guardrail Validation
        String enhancedPrompt = enhancePrompt(request.getPrompt(), request.getParameters());
        request.setPrompt(enhancedPrompt);

        // Step 3: Emit to Workers Topic
        kafkaTemplate.send(KafkaConstants.INFERENCE_WORKER_TOPIC, request.getRequestId(), request);
        log.info("[Orchestrator] Emitted enriched request {} to worker queue.", request.getRequestId());
    }

    private String enhancePrompt(String prompt, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return prompt;
        }
        StringBuilder enhanced = new StringBuilder(prompt);
        if (parameters.containsKey("systemPrompt")) {
            enhanced.insert(0, "[SYSTEM: " + parameters.get("systemPrompt") + "]\n\n");
        }
        if (parameters.containsKey("tone")) {
            enhanced.append("\n\n[Tone: ").append(parameters.get("tone")).append("]");
        }
        if (parameters.containsKey("style")) {
            enhanced.append("\n[Style: ").append(parameters.get("style")).append("]");
        }
        return enhanced.toString();
    }
}
