package com.codereview.platform.strategy;

import com.codereview.platform.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAnalysisStrategy implements AnalysisStrategy {

    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getStrategyName() {
        return "SECURITY";
    }

    @Override
    public List<Map<String, String>> analyze(String code, String language, Map<String, String> existingFiles) {
        try {
            String response;
            if (existingFiles != null && !existingFiles.isEmpty()) {
                response = aiService.analyzeWithContext(code, language, "SECURITY", existingFiles);
            } else {
                response = aiService.analyze(code, language, "SECURITY");
            }
            String cleaned = cleanResponse(response);
            List<Map<String, Object>> rawFindings = objectMapper.readValue(
                    cleaned,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            return convertFindings(rawFindings, "SECURITY");
        } catch (Exception e) {
            log.error("Security analysis failed: {}", e.getMessage());
            return List.of();
        }
    }

    private String cleanResponse(String response) {
        // AI sometimes wraps JSON in markdown code fences — strip them
        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
        }
        return cleaned;
    }

    private List<Map<String, String>> convertFindings(List<Map<String, Object>> raw, String strategy) {
        List<Map<String, String>> findings = new ArrayList<>();
        for (Map<String, Object> finding : raw) {
            Map<String, String> converted = new java.util.HashMap<>();
            converted.put("issue", String.valueOf(finding.getOrDefault("issue", "")));
            converted.put("severity", String.valueOf(finding.getOrDefault("severity", "MEDIUM")));
            converted.put("suggestion", String.valueOf(finding.getOrDefault("suggestion", "")));
            converted.put("confidence", String.valueOf(finding.getOrDefault("confidence", "0.5")));
            converted.put("line", String.valueOf(finding.getOrDefault("line", "null")));
            converted.put("strategy", strategy);
            findings.add(converted);
        }
        return findings;
    }
}