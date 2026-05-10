package com.codereview.platform.strategy;

import com.codereview.platform.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StyleAnalysisStrategy implements AnalysisStrategy {

    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getStrategyName() {
        return "STYLE";
    }

    @Override
    public List<Map<String, String>> analyze(String code, String language, Map<String, String> existingFiles) {
        try {
            String response;
            if (existingFiles != null && !existingFiles.isEmpty()) {
                response = aiService.analyzeWithContext(code, language, "STYLE", existingFiles);
            } else {
                response = aiService.analyze(code, language, "STYLE");
            }
            String cleaned = cleanResponse(response);
            List<Map<String, Object>> rawFindings = objectMapper.readValue(
                    cleaned,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            return convertFindings(rawFindings, "STYLE");
        } catch (Exception e) {
            log.error("Style analysis failed: {}", e.getMessage());
            return List.of();
        }
    }

    private String cleanResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
        }
        return cleaned;
    }

    private List<Map<String, String>> convertFindings(List<Map<String, Object>> raw, String strategy) {
        List<Map<String, String>> findings = new ArrayList<>();
        for (Map<String, Object> finding : raw) {
            Map<String, String> converted = new HashMap<>();
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