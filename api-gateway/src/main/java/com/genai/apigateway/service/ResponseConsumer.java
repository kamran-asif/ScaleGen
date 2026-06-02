package com.genai.apigateway.service;

import com.genai.common.dto.InferenceResponse;
import com.genai.common.util.KafkaConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResponseConsumer {

    private final GatewayService gatewayService;

    @KafkaListener(topics = KafkaConstants.INFERENCE_RESPONSE_TOPIC, groupId = "gateway-group")
    public void consumeResponse(InferenceResponse response) {
        gatewayService.processResponse(response);
    }
}
