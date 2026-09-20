package com.genai.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public class InferenceRequest implements Serializable {
    private String requestId;
    private String prompt;
    private String model;
    private Map<String, Object> parameters;
    private String userId;
    private String tenantId;
    private RoutingStrategy routingStrategy;
    private String idempotencyKey;
    private Double maxCostUsd;
    private String traceId;
    private String spanId;
    private LocalDateTime createdAt;
    private int retryCount;
    private int maxRetries;

    public InferenceRequest() {}

    public InferenceRequest(String requestId, String prompt, String model, Map<String, Object> parameters,
                            String userId, String tenantId, RoutingStrategy routingStrategy, String idempotencyKey,
                            Double maxCostUsd, String traceId, String spanId, LocalDateTime createdAt,
                            int retryCount, int maxRetries) {
        this.requestId = requestId;
        this.prompt = prompt;
        this.model = model;
        this.parameters = parameters;
        this.userId = userId;
        this.tenantId = tenantId;
        this.routingStrategy = routingStrategy;
        this.idempotencyKey = idempotencyKey;
        this.maxCostUsd = maxCostUsd;
        this.traceId = traceId;
        this.spanId = spanId;
        this.createdAt = createdAt;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String requestId;
        private String prompt;
        private String model;
        private Map<String, Object> parameters;
        private String userId;
        private String tenantId;
        private RoutingStrategy routingStrategy;
        private String idempotencyKey;
        private Double maxCostUsd;
        private String traceId;
        private String spanId;
        private LocalDateTime createdAt;
        private int retryCount;
        private int maxRetries;

        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder prompt(String prompt) { this.prompt = prompt; return this; }
        public Builder model(String model) { this.model = model; return this; }
        public Builder parameters(Map<String, Object> parameters) { this.parameters = parameters; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder routingStrategy(RoutingStrategy routingStrategy) { this.routingStrategy = routingStrategy; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder maxCostUsd(Double maxCostUsd) { this.maxCostUsd = maxCostUsd; return this; }
        public Builder traceId(String traceId) { this.traceId = traceId; return this; }
        public Builder spanId(String spanId) { this.spanId = spanId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder retryCount(int retryCount) { this.retryCount = retryCount; return this; }
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }

        public InferenceRequest build() {
            return new InferenceRequest(requestId, prompt, model, parameters, userId, tenantId, routingStrategy,
                    idempotencyKey, maxCostUsd, traceId, spanId, createdAt, retryCount, maxRetries);
        }
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public RoutingStrategy getRoutingStrategy() { return routingStrategy; }
    public void setRoutingStrategy(RoutingStrategy routingStrategy) { this.routingStrategy = routingStrategy; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Double getMaxCostUsd() { return maxCostUsd; }
    public void setMaxCostUsd(Double maxCostUsd) { this.maxCostUsd = maxCostUsd; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}
