package com.genai.apigateway.entity;

import com.genai.common.dto.RequestStatus;
import com.genai.common.dto.RoutingStrategy;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_tracking")
public class RequestTracking {
    @Id
    private String requestId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    private String model;
    private String selectedModel;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    private RoutingStrategy routingStrategy;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String userId;
    private String tenantId;
    private String idempotencyKey;
    private String traceId;

    private int retryCount;
    private int maxRetries;
    private int processingTimeMs;

    private Integer promptTokens;
    private Integer completionTokens;
    private Double costUsd;
    private String fallbackChain;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RequestTracking() {}

    public RequestTracking(String requestId, String prompt, String model, String selectedModel,
                           RequestStatus status, RoutingStrategy routingStrategy, String response,
                           String errorMessage, String userId, String tenantId, String idempotencyKey,
                           String traceId, int retryCount, int maxRetries, int processingTimeMs,
                           Integer promptTokens, Integer completionTokens, Double costUsd, String fallbackChain,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.requestId = requestId;
        this.prompt = prompt;
        this.model = model;
        this.selectedModel = selectedModel;
        this.status = status;
        this.routingStrategy = routingStrategy;
        this.response = response;
        this.errorMessage = errorMessage;
        this.userId = userId;
        this.tenantId = tenantId;
        this.idempotencyKey = idempotencyKey;
        this.traceId = traceId;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.processingTimeMs = processingTimeMs;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.costUsd = costUsd;
        this.fallbackChain = fallbackChain;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String requestId;
        private String prompt;
        private String model;
        private String selectedModel;
        private RequestStatus status;
        private RoutingStrategy routingStrategy;
        private String response;
        private String errorMessage;
        private String userId;
        private String tenantId;
        private String idempotencyKey;
        private String traceId;
        private int retryCount;
        private int maxRetries;
        private int processingTimeMs;
        private Integer promptTokens;
        private Integer completionTokens;
        private Double costUsd;
        private String fallbackChain;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder prompt(String prompt) { this.prompt = prompt; return this; }
        public Builder model(String model) { this.model = model; return this; }
        public Builder selectedModel(String selectedModel) { this.selectedModel = selectedModel; return this; }
        public Builder status(RequestStatus status) { this.status = status; return this; }
        public Builder routingStrategy(RoutingStrategy routingStrategy) { this.routingStrategy = routingStrategy; return this; }
        public Builder response(String response) { this.response = response; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder traceId(String traceId) { this.traceId = traceId; return this; }
        public Builder retryCount(int retryCount) { this.retryCount = retryCount; return this; }
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
        public Builder processingTimeMs(int processingTimeMs) { this.processingTimeMs = processingTimeMs; return this; }
        public Builder promptTokens(Integer promptTokens) { this.promptTokens = promptTokens; return this; }
        public Builder completionTokens(Integer completionTokens) { this.completionTokens = completionTokens; return this; }
        public Builder costUsd(Double costUsd) { this.costUsd = costUsd; return this; }
        public Builder fallbackChain(String fallbackChain) { this.fallbackChain = fallbackChain; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public RequestTracking build() {
            return new RequestTracking(requestId, prompt, model, selectedModel, status, routingStrategy,
                    response, errorMessage, userId, tenantId, idempotencyKey, traceId, retryCount, maxRetries,
                    processingTimeMs, promptTokens, completionTokens, costUsd, fallbackChain, createdAt, updatedAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSelectedModel() { return selectedModel; }
    public void setSelectedModel(String selectedModel) { this.selectedModel = selectedModel; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public RoutingStrategy getRoutingStrategy() { return routingStrategy; }
    public void setRoutingStrategy(RoutingStrategy routingStrategy) { this.routingStrategy = routingStrategy; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public int getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(int processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

    public Double getCostUsd() { return costUsd; }
    public void setCostUsd(Double costUsd) { this.costUsd = costUsd; }

    public String getFallbackChain() { return fallbackChain; }
    public void setFallbackChain(String fallbackChain) { this.fallbackChain = fallbackChain; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
