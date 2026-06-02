package com.genai.apigateway.controller;

import com.genai.apigateway.entity.RequestTracking;
import com.genai.apigateway.service.GatewayService;
import com.genai.common.dto.InferenceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inference")
@RequiredArgsConstructor
public class InferenceController {

    private final GatewayService gatewayService;

    @PostMapping
    public ResponseEntity<InferenceRequest> submitRequest(@RequestBody Map<String, Object> requestBody) {
        String prompt = (String) requestBody.get("prompt");
        String model = (String) requestBody.getOrDefault("model", "gpt-3.5-turbo");
        Map<String, Object> parameters = (Map<String, Object>) requestBody.getOrDefault("parameters", Map.of());
        String userId = (String) requestBody.getOrDefault("userId", "anonymous");

        InferenceRequest request = gatewayService.submitRequest(prompt, model, parameters, userId);
        return ResponseEntity.accepted().body(request);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RequestTracking> getRequestStatus(@PathVariable String requestId) {
        Optional<RequestTracking> tracking = gatewayService.getRequestStatus(requestId);
        return tracking.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
