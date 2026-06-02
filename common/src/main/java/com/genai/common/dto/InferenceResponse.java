package com.genai.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceResponse {
    private String requestId;
    private String response;
    private String model;
    private LocalDateTime processedAt;
    private boolean success;
    private String errorMessage;
    private int processingTimeMs;
}
