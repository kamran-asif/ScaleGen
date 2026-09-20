package com.genai.responseprocessor.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class SanitizerService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    public String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String sanitized = EMAIL_PATTERN.matcher(text).replaceAll("[REDACTED_EMAIL]");
        sanitized = SSN_PATTERN.matcher(sanitized).replaceAll("[REDACTED_SSN]");
        sanitized = CREDIT_CARD_PATTERN.matcher(sanitized).replaceAll("[REDACTED_CARD]");
        return sanitized.trim();
    }
}
