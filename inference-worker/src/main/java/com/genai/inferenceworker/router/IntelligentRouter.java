package com.genai.inferenceworker.router;

import com.genai.common.dto.RoutingStrategy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IntelligentRouter {

    private final Map<String, ModelSpec> catalog = new LinkedHashMap<>();

    public IntelligentRouter() {
        // Model A: High Quality / Complex Reasoning (e.g. GPT-4o)
        catalog.put("gpt-4o", ModelSpec.builder()
                .id("gpt-4o")
                .name("GPT-4o (Model A)")
                .provider("OpenAI")
                .costPer1kInputUsd(0.005)
                .costPer1kOutputUsd(0.015)
                .avgLatencyMs(450)
                .qualityScore(0.98)
                .maxContextTokens(128000)
                .build());

        // Model B: Balanced / Fast Coding & Analysis (e.g. Claude 3.5 Sonnet)
        catalog.put("claude-3-5-sonnet", ModelSpec.builder()
                .id("claude-3-5-sonnet")
                .name("Claude 3.5 Sonnet (Model B)")
                .provider("Anthropic")
                .costPer1kInputUsd(0.003)
                .costPer1kOutputUsd(0.012)
                .avgLatencyMs(320)
                .qualityScore(0.95)
                .maxContextTokens(200000)
                .build());

        // Model C: Fast & Low Cost (e.g. Llama 3 70B / Mistral)
        catalog.put("llama-3-70b", ModelSpec.builder()
                .id("llama-3-70b")
                .name("Llama 3 70B (Model C)")
                .provider("Meta / Ollama")
                .costPer1kInputUsd(0.0008)
                .costPer1kOutputUsd(0.002)
                .avgLatencyMs(180)
                .qualityScore(0.88)
                .maxContextTokens(8192)
                .build());
    }

    public List<ModelSpec> selectRoute(String requestedModel, RoutingStrategy strategy, String prompt) {
        List<ModelSpec> candidates = new ArrayList<>(catalog.values());

        if (strategy == RoutingStrategy.DIRECT && requestedModel != null && catalog.containsKey(requestedModel)) {
            ModelSpec selected = catalog.get(requestedModel);
            List<ModelSpec> route = new ArrayList<>();
            route.add(selected);
            for (ModelSpec m : candidates) {
                if (!m.getId().equals(selected.getId())) {
                    route.add(m);
                }
            }
            return route;
        }

        switch (strategy != null ? strategy : RoutingStrategy.AUTO) {
            case COST:
                candidates.sort(Comparator.comparingDouble(ModelSpec::getCostPer1kInputUsd));
                break;

            case QUALITY:
                candidates.sort(Comparator.comparingDouble(ModelSpec::getQualityScore).reversed());
                break;

            case LATENCY:
                candidates.sort(Comparator.comparingInt(ModelSpec::getAvgLatencyMs));
                break;

            case AUTO:
            default:
                // Auto multi-criteria dynamic scoring: evaluate prompt length & complexity
                int wordCount = prompt != null ? prompt.split("\\s+").length : 0;
                boolean isComplex = wordCount > 150 || (prompt != null && (prompt.contains("code") || prompt.contains("architecture") || prompt.contains("explain")));
                
                if (isComplex) {
                    candidates.sort(Comparator.comparingDouble(ModelSpec::getQualityScore).reversed());
                } else {
                    // Balance latency and cost for standard queries
                    candidates.sort((m1, m2) -> {
                        double score1 = (m1.getCostPer1kInputUsd() * 1000) + (m1.getAvgLatencyMs() / 100.0);
                        double score2 = (m2.getCostPer1kInputUsd() * 1000) + (m2.getAvgLatencyMs() / 100.0);
                        return Double.compare(score1, score2);
                    });
                }
                break;
        }

        return candidates;
    }

    public ModelSpec getModelSpec(String modelId) {
        return catalog.getOrDefault(modelId, catalog.get("gpt-4o"));
    }
}
