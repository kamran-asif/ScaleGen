package com.genai.common.dto;

import java.io.Serializable;

public class TokenUsage implements Serializable {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private double estimatedCostUsd;

    public TokenUsage() {}

    public TokenUsage(int promptTokens, int completionTokens, int totalTokens, double estimatedCostUsd) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.estimatedCostUsd = estimatedCostUsd;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        private double estimatedCostUsd;

        public Builder promptTokens(int promptTokens) { this.promptTokens = promptTokens; return this; }
        public Builder completionTokens(int completionTokens) { this.completionTokens = completionTokens; return this; }
        public Builder totalTokens(int totalTokens) { this.totalTokens = totalTokens; return this; }
        public Builder estimatedCostUsd(double estimatedCostUsd) { this.estimatedCostUsd = estimatedCostUsd; return this; }

        public TokenUsage build() {
            return new TokenUsage(promptTokens, completionTokens, totalTokens, estimatedCostUsd);
        }
    }

    public static TokenUsage calculate(String prompt, String response, double costPer1kInput, double costPer1kOutput) {
        int pTokens = Math.max(1, prompt != null ? prompt.split("\\s+").length * 4 / 3 : 0);
        int cTokens = Math.max(1, response != null ? response.split("\\s+").length * 4 / 3 : 0);
        int tTokens = pTokens + cTokens;
        double cost = ((pTokens / 1000.0) * costPer1kInput) + ((cTokens / 1000.0) * costPer1kOutput);
        return TokenUsage.builder()
                .promptTokens(pTokens)
                .completionTokens(cTokens)
                .totalTokens(tTokens)
                .estimatedCostUsd(Math.round(cost * 100000.0) / 100000.0)
                .build();
    }

    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }

    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }

    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }

    public double getEstimatedCostUsd() { return estimatedCostUsd; }
    public void setEstimatedCostUsd(double estimatedCostUsd) { this.estimatedCostUsd = estimatedCostUsd; }
}
