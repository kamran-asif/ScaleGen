package com.genai.apigateway.service;

import com.genai.common.dto.InferenceResponse;
import com.genai.common.util.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ResponseConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResponseConsumer.class);

    private final GatewayService gatewayService;

    public ResponseConsumer(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @KafkaListener(topics = KafkaConstants.INFERENCE_RESPONSE_TOPIC, groupId = "gateway-group")
    public void consumeResponse(InferenceResponse response) {
        log.info("[ResponseConsumer] Received inference response for requestId: {}", response.getRequestId());
        gatewayService.processResponse(response);
    }
}
