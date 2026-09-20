package com.genai.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class InferenceResponse implements Serializable {
    private String requestId;
    private String response;
    private String model;
    private String selectedModel;
    private LocalDateTime processedAt;
    private boolean success;
    private String errorMessage;
    private int processingTimeMs;
    private TokenUsage tokenUsage;
    private List<String> fallbackChain;
    private String circuitBreakerState;
    private boolean sanitized;
    private boolean cached;
    private String traceId;
    private String spanId;

    public InferenceResponse() {}

    public InferenceResponse(String requestId, String response, String model, String selectedModel,
                             LocalDateTime processedAt, boolean success, String errorMessage, int processingTimeMs,
                             TokenUsage tokenUsage, List<String> fallbackChain, String circuitBreakerState,
                             boolean sanitized, boolean cached, String traceId, String spanId) {
        this.requestId = requestId;
        this.response = response;
        this.model = model;
        this.selectedModel = selectedModel;
        this.processedAt = processedAt;
        this.success = success;
        this.errorMessage = errorMessage;
        this.processingTimeMs = processingTimeMs;
        this.tokenUsage = tokenUsage;
        this.fallbackChain = fallbackChain;
        this.circuitBreakerState = circuitBreakerState;
        this.sanitized = sanitized;
        this.cached = cached;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String requestId;
        private String response;
        private String model;
        private String selectedModel;
        private LocalDateTime processedAt;
        private boolean success;
        private String errorMessage;
        private int processingTimeMs;
        private TokenUsage tokenUsage;
        private List<String> fallbackChain;
        private String circuitBreakerState;
        private boolean sanitized;
        private boolean cached;
        private String traceId;
        private String spanId;

        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder response(String response) { this.response = response; return this; }
        public Builder model(String model) { this.model = model; return this; }
        public Builder selectedModel(String selectedModel) { this.selectedModel = selectedModel; return this; }
        public Builder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder processingTimeMs(int processingTimeMs) { this.processingTimeMs = processingTimeMs; return this; }
        public Builder tokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; return this; }
        public Builder fallbackChain(List<String> fallbackChain) { this.fallbackChain = fallbackChain; return this; }
        public Builder circuitBreakerState(String circuitBreakerState) { this.circuitBreakerState = circuitBreakerState; return this; }
        public Builder sanitized(boolean sanitized) { this.sanitized = sanitized; return this; }
        public Builder cached(boolean cached) { this.cached = cached; return this; }
        public Builder traceId(String traceId) { this.traceId = traceId; return this; }
        public Builder spanId(String spanId) { this.spanId = spanId; return this; }

        public InferenceResponse build() {
            return new InferenceResponse(requestId, response, model, selectedModel, processedAt, success,
                    errorMessage, processingTimeMs, tokenUsage, fallbackChain, circuitBreakerState, sanitized, cached, traceId, spanId);
        }
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSelectedModel() { return selectedModel; }
    public void setSelectedModel(String selectedModel) { this.selectedModel = selectedModel; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public int getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(int processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public TokenUsage getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; }

    public List<String> getFallbackChain() { return fallbackChain; }
    public void setFallbackChain(List<String> fallbackChain) { this.fallbackChain = fallbackChain; }

    public String getCircuitBreakerState() { return circuitBreakerState; }
    public void setCircuitBreakerState(String circuitBreakerState) { this.circuitBreakerState = circuitBreakerState; }

    public boolean isSanitized() { return sanitized; }
    public void setSanitized(boolean sanitized) { this.sanitized = sanitized; }

    public boolean isCached() { return cached; }
    public void setCached(boolean cached) { this.cached = cached; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }
}
