package com.genai.responseprocessor.service;

import com.genai.common.dto.InferenceResponse;
import com.genai.common.util.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ResponseProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ResponseProcessingService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SanitizerService sanitizerService;
    private final RedisTemplate<String, Object> redisTemplate;

    public ResponseProcessingService(KafkaTemplate<String, Object> kafkaTemplate,
                                     SanitizerService sanitizerService,
                                     RedisTemplate<String, Object> redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.sanitizerService = sanitizerService;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = KafkaConstants.RESPONSE_PROCESSING_TOPIC, groupId = "response-processor-group")
    public void processResponse(InferenceResponse response) {
        log.info("[ResponseProcessor] Cleaning and persisting response for requestId: {}", response.getRequestId());

        if (response.isSuccess() && response.getResponse() != null) {
            String raw = response.getResponse();
            String cleaned = sanitizerService.sanitize(raw);
            boolean wasSanitized = !cleaned.equals(raw.trim());

            response.setResponse(cleaned);
            response.setSanitized(wasSanitized);

            try {
                redisTemplate.opsForValue().set("response:payload:" + response.getRequestId(), cleaned, 12, TimeUnit.HOURS);
                log.info("[MultiTierStorage] Cached payload to Redis for requestId: {}", response.getRequestId());
            } catch (Exception e) {
                log.warn("[MultiTierStorage] Redis cache write failed: {}", e.getMessage());
            }
        }

        kafkaTemplate.send(KafkaConstants.INFERENCE_RESPONSE_TOPIC, response.getRequestId(), response);
        log.info("[ResponseProcessor] Completed response processing. Emitted to {} topic.", KafkaConstants.INFERENCE_RESPONSE_TOPIC);
    }
}
