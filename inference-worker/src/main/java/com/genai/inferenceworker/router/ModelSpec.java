package com.genai.inferenceworker.router;

public class ModelSpec {
    private String id;
    private String name;
    private String provider;
    private double costPer1kInputUsd;
    private double costPer1kOutputUsd;
    private int avgLatencyMs;
    private double qualityScore;
    private int maxContextTokens;

    public ModelSpec() {}

    public ModelSpec(String id, String name, String provider, double costPer1kInputUsd,
                     double costPer1kOutputUsd, int avgLatencyMs, double qualityScore, int maxContextTokens) {
        this.id = id;
        this.name = name;
        this.provider = provider;
        this.costPer1kInputUsd = costPer1kInputUsd;
        this.costPer1kOutputUsd = costPer1kOutputUsd;
        this.avgLatencyMs = avgLatencyMs;
        this.qualityScore = qualityScore;
        this.maxContextTokens = maxContextTokens;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String name;
        private String provider;
        private double costPer1kInputUsd;
        private double costPer1kOutputUsd;
        private int avgLatencyMs;
        private double qualityScore;
        private int maxContextTokens;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder costPer1kInputUsd(double costPer1kInputUsd) { this.costPer1kInputUsd = costPer1kInputUsd; return this; }
        public Builder costPer1kOutputUsd(double costPer1kOutputUsd) { this.costPer1kOutputUsd = costPer1kOutputUsd; return this; }
        public Builder avgLatencyMs(int avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; return this; }
        public Builder qualityScore(double qualityScore) { this.qualityScore = qualityScore; return this; }
        public Builder maxContextTokens(int maxContextTokens) { this.maxContextTokens = maxContextTokens; return this; }

        public ModelSpec build() {
            return new ModelSpec(id, name, provider, costPer1kInputUsd, costPer1kOutputUsd, avgLatencyMs, qualityScore, maxContextTokens);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public double getCostPer1kInputUsd() { return costPer1kInputUsd; }
    public void setCostPer1kInputUsd(double costPer1kInputUsd) { this.costPer1kInputUsd = costPer1kInputUsd; }

    public double getCostPer1kOutputUsd() { return costPer1kOutputUsd; }
    public void setCostPer1kOutputUsd(double costPer1kOutputUsd) { this.costPer1kOutputUsd = costPer1kOutputUsd; }

    public int getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(int avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }

    public double getQualityScore() { return qualityScore; }
    public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }

    public int getMaxContextTokens() { return maxContextTokens; }
    public void setMaxContextTokens(int maxContextTokens) { this.maxContextTokens = maxContextTokens; }
}
