package com.genai.paymentservice.dto;

import java.math.BigDecimal;

public class CreatePaymentRequest {
    private String userId;
    private BigDecimal amount;
    private String currency;
    private int credits;
    private String description;

    public CreatePaymentRequest() {}

    public CreatePaymentRequest(String userId, BigDecimal amount, String currency, int credits, String description) {
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.credits = credits;
        this.description = description;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
