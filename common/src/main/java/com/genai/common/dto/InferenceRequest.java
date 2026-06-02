package com.genai.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceRequest {
    private String requestId;
    private String prompt;
    private String model;
    private Map<String, Object> parameters;
    private String userId;
    private LocalDateTime createdAt;
    private int retryCount;
    private int maxRetries;
}
