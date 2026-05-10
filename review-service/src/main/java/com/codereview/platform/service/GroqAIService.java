package com.codereview.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GroqAIService implements AIService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String analyze(String code, String language, String analysisType) {
        try {
            String prompt = buildPrompt(code, language, analysisType);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are an expert code reviewer. Analyze code and return findings as a JSON array. Each finding must have: issue (string), severity (HIGH/MEDIUM/LOW), confidence (0.0-1.0), suggestion (string), line (integer or null)."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.1,
                    "max_tokens", 1024
            );

            String json = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.get("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Groq API error: {}", response.code());
                    return "[]";
                }
                Map<String, Object> responseMap = objectMapper.readValue(response.body().string(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            log.error("AI analysis failed: {}", e.getMessage());
            return "[]";
        }
    }

    private String buildPrompt(String code, String language, String analysisType) {
        String focus = getAnalysisFocus(analysisType);
        return String.format("""
        Analyze this %s code. %s
        Return ONLY a JSON array of findings, no other text.
        If no issues found, return empty array: []
        Each finding: {"issue": "description", "severity": "HIGH|MEDIUM|LOW", "confidence": 0.0-1.0, "suggestion": "how to fix", "line": integer or null if unknown}
        
        Code:
```%s
        %s
```
        """, language, focus, language, code);
    }

    private String getAnalysisFocus(String analysisType) {
        return switch (analysisType.toUpperCase()) {
            case "SECURITY" -> """
            Focus ONLY on security vulnerabilities: SQL injection, hardcoded credentials, \
            authentication issues, sensitive data exposure, insecure cryptography. \
            Do NOT report performance or style issues. \
            Always identify the exact line number for each finding.""";
            case "PERFORMANCE" -> """
            Focus ONLY on performance issues: algorithmic complexity (O(n²)+), memory leaks, \
            unnecessary object creation, inefficient loops, N+1 query patterns. \
            Do NOT report security vulnerabilities or style issues. \
            Always identify the exact line number for each finding.""";
            case "STYLE" -> """
            Focus ONLY on code style and maintainability: naming conventions, code duplication, \
            missing documentation, magic numbers, deep nesting, poor error handling structure. \
            Do NOT report security vulnerabilities or performance issues. \
            Always identify the exact line number for each finding.""";
            default -> """
            Analyze for security vulnerabilities, performance issues, and code style problems. \
            Always identify the exact line number for each finding.""";
        };
    }

    @Override
    public String analyzeWithContext(String code, String language, String analysisType, Map<String, String> existingFiles) {
        try {
            String prompt = buildContextAwarePrompt(code, language, analysisType, existingFiles);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are an expert code reviewer with deep knowledge of software architecture and design patterns. " +
                                            "Analyze code for issues AND consistency with the existing codebase. " +
                                            "Return ONLY a JSON array of findings, no other text. " +
                                            "Each finding must have: issue (string), severity (HIGH/MEDIUM/LOW), confidence (0.0-1.0), suggestion (string), line (integer or null), type (BUG/SECURITY/PERFORMANCE/STYLE/CONSISTENCY)."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.1,
                    "max_tokens", 2048
            );

            String json = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.get("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Groq API error: {}", response.code());
                    return "[]";
                }
                Map<String, Object> responseMap = objectMapper.readValue(response.body().string(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            log.error("AI analysis with context failed: {}", e.getMessage());
            return "[]";
        }
    }

    private String buildContextAwarePrompt(String code, String language, String analysisType, Map<String, String> existingFiles) {
        StringBuilder contextSection = new StringBuilder();

        if (existingFiles != null && !existingFiles.isEmpty()) {
            contextSection.append("EXISTING CODEBASE CONTEXT:\n");
            contextSection.append("The following files already exist in this repository. ");
            contextSection.append("Use them to understand the established patterns, naming conventions, ");
            contextSection.append("error handling style, and architecture.\n\n");

            for (Map.Entry<String, String> entry : existingFiles.entrySet()) {
                contextSection.append("File: ").append(entry.getKey()).append("\n");
                contextSection.append("```").append(language).append("\n");
                contextSection.append(entry.getValue()).append("\n");
                contextSection.append("```\n\n");
            }
        }

        return String.format("""
            %s
            NEW CODE BEING REVIEWED:
            Analyze this %s code. %s
            Also check if it is consistent with the existing codebase patterns above.
            Return ONLY a JSON array of findings, no other text.
            If no issues found, return empty array: []
            Each finding: {"issue": "description", "severity": "HIGH|MEDIUM|LOW", "confidence": 0.0-1.0, "suggestion": "how to fix", "line": integer or null if unknown}
            
        ```%s
            %s
        ```
            """, contextSection.toString(), language, getAnalysisFocus(analysisType), language, code);
    }
}