package com.genai.apigateway.service;

import com.genai.apigateway.entity.RequestTracking;
import com.genai.apigateway.repository.RequestTrackingRepository;
import com.genai.common.dto.InferenceRequest;
import com.genai.common.dto.InferenceResponse;
import com.genai.common.dto.RequestStatus;
import com.genai.common.dto.RoutingStrategy;
import com.genai.common.util.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RequestTrackingRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;

    public GatewayService(KafkaTemplate<String, Object> kafkaTemplate,
                          RequestTrackingRepository repository,
                          RedisTemplate<String, Object> redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    public InferenceRequest submitRequest(String prompt, String model, Map<String, Object> parameters,
                                          String userId, String tenantId, RoutingStrategy strategy, String idempotencyKey) {
        String requestId = "req-" + UUID.randomUUID().toString().substring(0, 8);
        String traceId = "trace-" + UUID.randomUUID().toString().substring(0, 12);
        String spanId = "span-" + UUID.randomUUID().toString().substring(0, 8);

        RoutingStrategy routingStrategy = strategy != null ? strategy : RoutingStrategy.AUTO;

        InferenceRequest request = InferenceRequest.builder()
                .requestId(requestId)
                .prompt(prompt)
                .model(model != null ? model : "gpt-4o")
                .parameters(parameters != null ? parameters : Map.of())
                .userId(userId != null ? userId : "user-default")
                .tenantId(tenantId != null ? tenantId : "tenant-default")
                .routingStrategy(routingStrategy)
                .idempotencyKey(idempotencyKey)
                .traceId(traceId)
                .spanId(spanId)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .maxRetries(3)
                .build();

        RequestTracking tracking = RequestTracking.builder()
                .requestId(requestId)
                .prompt(prompt)
                .model(request.getModel())
                .userId(request.getUserId())
                .tenantId(request.getTenantId())
                .routingStrategy(routingStrategy)
                .idempotencyKey(idempotencyKey)
                .traceId(traceId)
                .status(RequestStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();
        repository.save(tracking);

        kafkaTemplate.send(KafkaConstants.INFERENCE_REQUEST_TOPIC, requestId, request);
        log.info("[API Gateway] Received & Enqueued request {} with strategy {} and traceId {}", requestId, routingStrategy, traceId);

        redisTemplate.opsForValue().set("request:" + requestId, RequestStatus.PENDING, 1, TimeUnit.HOURS);

        return request;
    }

    public Optional<RequestTracking> getRequestStatus(String requestId) {
        return repository.findById(requestId);
    }

    public List<RequestTracking> getAllRequests() {
        return repository.findAll();
    }

    public void processResponse(InferenceResponse response) {
        Optional<RequestTracking> optionalTracking = repository.findById(response.getRequestId());
        if (optionalTracking.isPresent()) {
            RequestTracking tracking = optionalTracking.get();
            tracking.setStatus(response.isSuccess() ? RequestStatus.COMPLETED : RequestStatus.FAILED);
            tracking.setSelectedModel(response.getSelectedModel() != null ? response.getSelectedModel() : response.getModel());
            tracking.setResponse(response.getResponse());
            tracking.setErrorMessage(response.getErrorMessage());
            tracking.setProcessingTimeMs(response.getProcessingTimeMs());
            
            if (response.getTokenUsage() != null) {
                tracking.setPromptTokens(response.getTokenUsage().getPromptTokens());
                tracking.setCompletionTokens(response.getTokenUsage().getCompletionTokens());
                tracking.setCostUsd(response.getTokenUsage().getEstimatedCostUsd());
            }
            if (response.getFallbackChain() != null) {
                tracking.setFallbackChain(String.join(" -> ", response.getFallbackChain()));
            }

            repository.save(tracking);
            redisTemplate.opsForValue().set("request:" + response.getRequestId(), tracking, 1, TimeUnit.HOURS);
            log.info("[API Gateway] Completed request {} - Status: {}, Model: {}", response.getRequestId(), tracking.getStatus(), tracking.getSelectedModel());
        }
    }
}
