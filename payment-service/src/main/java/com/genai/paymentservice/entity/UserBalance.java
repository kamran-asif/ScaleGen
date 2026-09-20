package com.genai.paymentservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_balances")
public class UserBalance {
    @Id
    private String userId;

    private int credits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserBalance() {}

    public UserBalance(String userId, int credits, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.credits = credits;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userId;
        private int credits;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder credits(int credits) { this.credits = credits; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserBalance build() {
            return new UserBalance(userId, credits, createdAt, updatedAt);
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

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
