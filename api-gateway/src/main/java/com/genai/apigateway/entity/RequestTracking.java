package com.genai.apigateway.entity;

import com.genai.common.dto.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestTracking {
    @Id
    private String requestId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    private String model;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String userId;

    private int retryCount;

    private int maxRetries;

    private int processingTimeMs;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
