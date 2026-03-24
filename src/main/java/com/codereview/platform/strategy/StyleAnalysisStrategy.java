package com.codereview.platform.strategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StyleAnalysisStrategy implements AnalysisStrategy {

    @Override
    public String getStrategyName() {
        return "STYLE";
    }

    @Override
    public List<Map<String, String>> analyze(String code, String language) {
        List<Map<String, String>> findings = new ArrayList<>();
        String[] lines = code.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            int lineNumber = i + 1;

            // Lines over 120 chars are hard to read during code review
            if (line.length() > 120) {
                findings.add(createFinding(
                        "Line exceeds 120 characters (" + line.length() + " chars)",
                        "LOW",
                        String.valueOf(lineNumber),
                        "Break long lines for readability"
                ));
            }

            // TODO/FIXME/HACK comments left in production code
            if (trimmed.contains("TODO") || trimmed.contains("FIXME") || trimmed.contains("HACK")) {
                findings.add(createFinding(
                        "Unresolved annotation found: " + trimmed,
                        "LOW",
                        String.valueOf(lineNumber),
                        "Resolve this or track it in your issue tracker"
                ));
            }

            // Magic numbers make code hard to understand
            if (!trimmed.startsWith("//") && !trimmed.startsWith("*")) {
                if (trimmed.matches(".*[^a-zA-Z0-9_](\\d{3,})[^a-zA-Z0-9_].*")) {
                    findings.add(createFinding(
                            "Magic number detected",
                            "LOW",
                            String.valueOf(lineNumber),
                            "Extract into a named constant for clarity"
                    ));
                }
            }

            // Deeply nested code — count leading spaces as a proxy for nesting level
            int indentSpaces = 0;
            for (char c : line.toCharArray()) {
                if (c == ' ') indentSpaces++;
                else break;
            }
            if (indentSpaces >= 20 && !trimmed.isEmpty()) { // ~5 levels at 4 spaces each
                findings.add(createFinding(
                        "Deeply nested code (indent level ~" + (indentSpaces / 4) + ")",
                        "MEDIUM",
                        String.valueOf(lineNumber),
                        "Extract to a private method to reduce nesting"
                ));
            }
        }

        return findings;
    }

    private Map<String, String> createFinding(String issue, String severity, String line, String suggestion) {
        Map<String, String> finding = new HashMap<>();
        finding.put("issue", issue);
        finding.put("severity", severity);
        finding.put("line", line);
        finding.put("suggestion", suggestion);
        finding.put("strategy", getStrategyName());
        return finding;
    }
}