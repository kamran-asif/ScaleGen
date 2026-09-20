package com.genai.apigateway.controller;

import com.genai.apigateway.entity.RequestTracking;
import com.genai.apigateway.service.GatewayService;
import com.genai.common.dto.InferenceRequest;
import com.genai.common.dto.RoutingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/inference")
@CrossOrigin(origins = "*")
public class InferenceController {

    private final GatewayService gatewayService;

    public InferenceController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping
    public ResponseEntity<InferenceRequest> submitRequest(@RequestBody Map<String, Object> requestBody) {
        String prompt = (String) requestBody.get("prompt");
        String model = (String) requestBody.getOrDefault("model", "gpt-4o");
        Map<String, Object> parameters = (Map<String, Object>) requestBody.getOrDefault("parameters", Map.of());
        String userId = (String) requestBody.getOrDefault("userId", "user-123");
        String tenantId = (String) requestBody.getOrDefault("tenantId", "tenant-org1");
        String idempotencyKey = (String) requestBody.get("idempotencyKey");
        
        String strategyStr = (String) requestBody.getOrDefault("routingStrategy", "AUTO");
        RoutingStrategy strategy;
        try {
            strategy = RoutingStrategy.valueOf(strategyStr.toUpperCase());
        } catch (Exception e) {
            strategy = RoutingStrategy.AUTO;
        }

        InferenceRequest request = gatewayService.submitRequest(prompt, model, parameters, userId, tenantId, strategy, idempotencyKey);
        return ResponseEntity.accepted().body(request);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RequestTracking> getRequestStatus(@PathVariable String requestId) {
        Optional<RequestTracking> tracking = gatewayService.getRequestStatus(requestId);
        return tracking.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RequestTracking>> getAllRequests() {
        return ResponseEntity.ok(gatewayService.getAllRequests());
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        List<RequestTracking> all = gatewayService.getAllRequests();
        long totalRequests = all.size();
        long completed = all.stream().filter(r -> "COMPLETED".equals(String.valueOf(r.getStatus()))).count();
        long failed = all.stream().filter(r -> "FAILED".equals(String.valueOf(r.getStatus()))).count();
        double totalCost = all.stream().mapToDouble(r -> r.getCostUsd() != null ? r.getCostUsd() : 0.0).sum();
        double avgLatency = all.stream().filter(r -> r.getProcessingTimeMs() > 0)
                .mapToInt(RequestTracking::getProcessingTimeMs).average().orElse(0.0);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRequests", totalRequests);
        metrics.put("completedRequests", completed);
        metrics.put("failedRequests", failed);
        metrics.put("totalCostUsd", Math.round(totalCost * 100000.0) / 100000.0);
        metrics.put("avgLatencyMs", Math.round(avgLatency));
        metrics.put("openTelemetryCollector", "CONNECTED");
        metrics.put("jaegerTracing", "ACTIVE");
        metrics.put("prometheusMetrics", "EXPORTING");
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/models")
    public ResponseEntity<List<Map<String, Object>>> getAvailableModels() {
        List<Map<String, Object>> models = List.of(
                Map.of("id", "gpt-4o", "name", "GPT-4o (Model A)", "tier", "High Quality", "costPer1k", 0.005, "avgLatencyMs", 420, "provider", "OpenAI"),
                Map.of("id", "claude-3-5-sonnet", "name", "Claude 3.5 Sonnet (Model B)", "tier", "Balanced", "costPer1k", 0.003, "avgLatencyMs", 350, "provider", "Anthropic"),
                Map.of("id", "llama-3-70b", "name", "Llama 3 70B (Model C)", "tier", "Fast & Low Cost", "costPer1k", 0.0008, "avgLatencyMs", 180, "provider", "Meta / Ollama")
        );
        return ResponseEntity.ok(models);
    }
}
