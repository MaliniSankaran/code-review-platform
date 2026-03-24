package com.codereview.platform.strategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SecurityAnalysisStrategy implements AnalysisStrategy{

    public String getStrategyName(){
        return "SECURITY";
    }

    @Override
    public List<Map<String, String>> analyze(String code, String language) {
        List<Map<String, String>> findings = new ArrayList<>();
        String[] lines = code.split("\n");

        for(int i=0; i<lines.length; i++){
            String line = lines[i].trim();
            int lineNumber = i+1;

            if(line.contains("password") && line.contains("=")&& !line.contains("getPassword")){
                findings.add(createFinding(
                        "Possible harcoded password",
                        "HIGH",
                        String.valueOf(lineNumber),
                        "Use environment variables for sensitive values"
                ));
            }

            if (line.contains("SELECT") && line.contains("+")) {
                findings.add(createFinding(
                        "Possible SQL injection",
                        "CRITICAL",
                        String.valueOf(lineNumber),
                        "Use parameterized queries instead of string concatenation"
                ));
            }

            if (line.contains("System.out.println") || line.contains("print(")) {
                findings.add(createFinding(
                        "Debug print statement in code",
                        "LOW",
                        String.valueOf(lineNumber),
                        "Use a proper logging framework"
                ));
            }

            if (line.contains("catch") && line.contains("Exception") && lines.length > i + 1
                    && lines[i + 1].trim().isEmpty()) {
                findings.add(createFinding(
                        "Empty catch block",
                        "MEDIUM",
                        String.valueOf(lineNumber),
                        "Log the exception or handle it properly"
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

