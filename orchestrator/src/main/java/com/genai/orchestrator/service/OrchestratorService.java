package com.genai.orchestrator.service;

import com.genai.common.dto.InferenceRequest;
import com.genai.common.util.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestratorService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaConstants.INFERENCE_REQUEST_TOPIC, groupId = "orchestrator-group")
    public void orchestrateRequest(InferenceRequest request) {
        log.info("Orchestrating request: {}", request.getRequestId());

        String enhancedPrompt = enhancePrompt(request.getPrompt(), request.getParameters());
        request.setPrompt(enhancedPrompt);

        kafkaTemplate.send(KafkaConstants.INFERENCE_REQUEST_TOPIC + ".worker", request.getRequestId(), request);
    }

    private String enhancePrompt(String prompt, java.util.Map<String, Object> parameters) {
        StringBuilder enhanced = new StringBuilder(prompt);
        
        if (parameters.containsKey("tone")) {
            enhanced.append("\n\nTone: ").append(parameters.get("tone"));
        }
        if (parameters.containsKey("style")) {
            enhanced.append("\n\nStyle: ").append(parameters.get("style"));
        }
        if (parameters.containsKey("maxLength")) {
            enhanced.append("\n\nMax length: ").append(parameters.get("maxLength"));
        }
        
        return enhanced.toString();
    }
}
