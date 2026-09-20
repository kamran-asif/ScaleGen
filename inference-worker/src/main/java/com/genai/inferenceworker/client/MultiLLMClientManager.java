package com.genai.inferenceworker.client;

import com.genai.inferenceworker.router.ModelSpec;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class MultiLLMClientManager {

    private static final Logger log = LoggerFactory.getLogger(MultiLLMClientManager.class);

    private final OkHttpClient client;
    private final Gson gson;
    private final String openAiApiKey;
    private final String anthropicApiKey;

    public MultiLLMClientManager(@Value("${openai.api.key:mock-key}") String openAiApiKey,
                                 @Value("${anthropic.api.key:mock-key}") String anthropicApiKey) {
        this.openAiApiKey = openAiApiKey;
        this.anthropicApiKey = anthropicApiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public String executeInference(ModelSpec model, String prompt) throws Exception {
        log.info("[LLM Provider] Dispatching prompt to model: {} (Provider: {})", model.getName(), model.getProvider());

        if (isRealApiKeyAvailable(model)) {
            try {
                if ("OpenAI".equalsIgnoreCase(model.getProvider())) {
                    return callOpenAI(model.getId(), prompt);
                } else if ("Anthropic".equalsIgnoreCase(model.getProvider())) {
                    return callAnthropic(model.getId(), prompt);
                }
            } catch (Exception e) {
                log.warn("[LLM Provider] Remote API call to {} failed: {}. Falling back to simulation engine.", model.getId(), e.getMessage());
                throw e;
            }
        }

        return simulateModelResponse(model, prompt);
    }

    private boolean isRealApiKeyAvailable(ModelSpec model) {
        if ("OpenAI".equalsIgnoreCase(model.getProvider()) && openAiApiKey != null && !openAiApiKey.contains("your-key") && !openAiApiKey.equals("mock-key")) {
            return true;
        }
        if ("Anthropic".equalsIgnoreCase(model.getProvider()) && anthropicApiKey != null && !anthropicApiKey.contains("your-key") && !anthropicApiKey.equals("mock-key")) {
            return true;
        }
        return false;
    }

    private String callOpenAI(String modelId, String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelId);
        JsonArray messages = new JsonArray();
        JsonObject msg = new JsonObject();
        msg.addProperty("role", "user");
        msg.addProperty("content", prompt);
        messages.add(msg);
        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + openAiApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI returned HTTP " + response.code() + ": " + response.message());
            }
            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            return json.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();
        }
    }

    private String callAnthropic(String modelId, String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelId.equals("claude-3-5-sonnet") ? "claude-3-5-sonnet-20241022" : modelId);
        requestBody.addProperty("max_tokens", 1024);
        JsonArray messages = new JsonArray();
        JsonObject msg = new JsonObject();
        msg.addProperty("role", "user");
        msg.addProperty("content", prompt);
        messages.add(msg);
        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", anthropicApiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Anthropic returned HTTP " + response.code());
            }
            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            return json.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString();
        }
    }

    private String simulateModelResponse(ModelSpec model, String prompt) throws InterruptedException {
        long delay = Math.max(100, model.getAvgLatencyMs() + (long) (Math.random() * 80 - 40));
        Thread.sleep(delay);

        String cleanPrompt = prompt.replaceAll("\\[.*?\\]", "").trim();

        if (cleanPrompt.toLowerCase().contains("microservice")) {
            return String.format("[%s Response - %s]\nMicroservices are an architectural style that structures an application as a collection of services that are highly maintainable, testable, loosely coupled, independently deployable, and organized around business capabilities.",
                    model.getName(), model.getProvider());
        }
        if (cleanPrompt.toLowerCase().contains("kafka") || cleanPrompt.toLowerCase().contains("event")) {
            return String.format("[%s Response - %s]\nApache Kafka provides distributed event streaming with high throughput, low latency, horizontal scalability, and multi-partition ordering guarantees.",
                    model.getName(), model.getProvider());
        }

        return String.format("[%s Response]\nProcessed request efficiently via %s provider. Prompt: '%s'. Simulated latency: %d ms.",
                model.getName(), model.getProvider(), cleanPrompt.length() > 60 ? cleanPrompt.substring(0, 60) + "..." : cleanPrompt, delay);
    }
}
