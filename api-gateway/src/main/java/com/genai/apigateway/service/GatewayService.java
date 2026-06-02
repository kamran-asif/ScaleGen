package com.genai.apigateway.service;

import com.genai.apigateway.entity.RequestTracking;
import com.genai.apigateway.repository.RequestTrackingRepository;
import com.genai.common.dto.InferenceRequest;
import com.genai.common.dto.InferenceResponse;
import com.genai.common.dto.RequestStatus;
import com.genai.common.util.KafkaConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GatewayService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RequestTrackingRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;

    public InferenceRequest submitRequest(String prompt, String model, Map<String, Object> parameters, String userId) {
        String requestId = UUID.randomUUID().toString();
        
        InferenceRequest request = InferenceRequest.builder()
                .requestId(requestId)
                .prompt(prompt)
                .model(model)
                .parameters(parameters)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .maxRetries(3)
                .build();

        RequestTracking tracking = RequestTracking.builder()
                .requestId(requestId)
                .prompt(prompt)
                .model(model)
                .userId(userId)
                .status(RequestStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();
        repository.save(tracking);

        kafkaTemplate.send(KafkaConstants.INFERENCE_REQUEST_TOPIC, requestId, request);

        redisTemplate.opsForValue().set("request:" + requestId, RequestStatus.PENDING, 1, TimeUnit.HOURS);

        return request;
    }

    public Optional<RequestTracking> getRequestStatus(String requestId) {
        Object cached = redisTemplate.opsForValue().get("request:" + requestId);
        return repository.findById(requestId);
    }

    public void processResponse(InferenceResponse response) {
        Optional<RequestTracking> optionalTracking = repository.findById(response.getRequestId());
        if (optionalTracking.isPresent()) {
            RequestTracking tracking = optionalTracking.get();
            tracking.setStatus(response.isSuccess() ? RequestStatus.COMPLETED : RequestStatus.FAILED);
            tracking.setResponse(response.getResponse());
            tracking.setErrorMessage(response.getErrorMessage());
            tracking.setProcessingTimeMs(response.getProcessingTimeMs());
            repository.save(tracking);

            redisTemplate.opsForValue().set("request:" + response.getRequestId(), tracking, 1, TimeUnit.HOURS);
        }
    }
}
