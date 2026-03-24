package com.codereview.platform.strategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PerformanceAnalysisStrategy implements AnalysisStrategy{

    @Override
    public String getStrategyName() {
        return "PERFORMANCE";
    }

    @Override
    public List<Map<String, String>> analyze(String code, String language) {

        List<Map<String, String>> findings = new ArrayList<>();
        String[] lines = code.split("\n");

        int nestedLoopDepth = 0;

        for(int i = 0; i < lines.length; i++){
            String line = lines[i].trim();
            int lineNumber = i+1;

            if (line.contains("for") || line.contains("while")) {
                nestedLoopDepth++;
                if (nestedLoopDepth >= 2) {
                    findings.add(createFinding(
                            "Nested loop detected (O(n²) or worse)",
                            "HIGH",
                            String.valueOf(lineNumber),
                            "Consider using a HashMap or Set for O(1) lookups"
                    ));
                }
            }
            if (line.contains("}")) {
                if (nestedLoopDepth > 0) nestedLoopDepth--;
            }

            if (line.contains("new ArrayList") && line.contains("for")) {
                findings.add(createFinding(
                        "Creating collection inside loop",
                        "MEDIUM",
                        String.valueOf(lineNumber),
                        "Move collection creation outside the loop"
                ));
            }

            if (line.contains(".get(") && line.contains("for")) {
                findings.add(createFinding(
                        "Possible N+1 query pattern",
                        "HIGH",
                        String.valueOf(lineNumber),
                        "Consider batch fetching or JOIN queries"
                ));
            }
            if (line.contains("String ") && line.contains("+=")) {
                findings.add(createFinding(
                        "String concatenation in possible loop",
                        "MEDIUM",
                        String.valueOf(lineNumber),
                        "Use StringBuilder for better performance"
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
