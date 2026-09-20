package com.genai.common.util;

public class KafkaConstants {
    public static final String INFERENCE_REQUEST_TOPIC = "inference-requests";
    public static final String INFERENCE_WORKER_TOPIC = "inference-requests.worker";
    public static final String INFERENCE_RESPONSE_TOPIC = "inference-responses";
    public static final String RESPONSE_PROCESSING_TOPIC = "response-processing";
    public static final String RETRY_TOPIC = "inference-retries";
    public static final String DLQ_TOPIC = "inference-dlq";
}
