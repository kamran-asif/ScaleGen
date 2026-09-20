package com.genai.paymentservice.dto;

public class CreatePaymentResponse {
    private String paymentId;
    private String clientSecret;
    private String status;

    public CreatePaymentResponse() {}

    public CreatePaymentResponse(String paymentId, String clientSecret, String status) {
        this.paymentId = paymentId;
        this.clientSecret = clientSecret;
        this.status = status;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String paymentId;
        private String clientSecret;
        private String status;

        public Builder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public Builder clientSecret(String clientSecret) { this.clientSecret = clientSecret; return this; }
        public Builder status(String status) { this.status = status; return this; }

        public CreatePaymentResponse build() {
            return new CreatePaymentResponse(paymentId, clientSecret, status);
        }
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
