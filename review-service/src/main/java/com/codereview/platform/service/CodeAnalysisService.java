package com.codereview.platform.service;

import com.codereview.platform.client.FileServiceClient;
import com.codereview.platform.strategy.AnalysisContext;
import com.codereview.platform.strategy.AnalysisStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeAnalysisService {

    private final List<AnalysisStrategy> strategies;
    private final AnalysisContext analysisContext;
    private final FileServiceClient fileServiceClient;

    public List<Map<String, String>> analyze(String code, String language, String analysisType) {
        return analyze(code, language, analysisType, null);
    }

    public List<Map<String, String>> analyze(String code, String language, String analysisType, Map<String, String> existingFiles) {
        analysisContext.setExistingFiles(existingFiles);
        try {
            if ("ALL".equalsIgnoreCase(analysisType)) {
                return runAllStrategies(code, language);
            }
            return runSingleStrategy(code, language, analysisType);
        } finally {
            analysisContext.clearExistingFiles();
        }
    }

    private List<Map<String, String>> runAllStrategies(String code, String language) {
        List<Map<String, String>> allFindings = new ArrayList<>();
        for (AnalysisStrategy strategy : strategies) {
            analysisContext.setStrategy(strategy);
            allFindings.addAll(analysisContext.executeAnalysis(code, language));
        }
        return deduplicate(allFindings);
    }

    private List<Map<String, String>> runSingleStrategy(String code, String language, String strategyName) {
        AnalysisStrategy strategy = strategies.stream()
                .filter(s -> s.getStrategyName().equalsIgnoreCase(strategyName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown analysis type: " + strategyName + ". Valid: SECURITY, PERFORMANCE, STYLE, ALL"
                ));

        // This is the Strategy Pattern in action — set, then execute via context
        analysisContext.setStrategy(strategy);
        return analysisContext.executeAnalysis(code, language);
    }

    private String normalizeIssue(String issue) {
        // Remove common stop words and punctuation, keep meaningful words
        Set<String> stopWords = Set.of("a", "an", "the", "in", "on", "at", "to", "for",
                "of", "and", "or", "is", "are", "was", "be", "this", "that", "with",
                "due", "via", "using", "use", "may", "can", "could", "should", "potential",
                "possible", "detected", "found");

        return Arrays.stream(issue.toLowerCase().replaceAll("[^a-z0-9 ]", "").split(" "))
                .filter(w -> !w.isBlank() && !stopWords.contains(w) && w.length() > 2)
                .sorted()
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private boolean isSimilar(String issue1, String issue2) {
        Set<String> words1 = new java.util.HashSet<>(Arrays.asList(normalizeIssue(issue1).split(" ")));
        Set<String> words2 = new java.util.HashSet<>(Arrays.asList(normalizeIssue(issue2).split(" ")));
        if (words1.isEmpty() || words2.isEmpty()) return false;

        Set<String> intersection = new java.util.HashSet<>(words1);
        intersection.retainAll(words2);

        // Jaccard similarity — intersection / union
        Set<String> union = new java.util.HashSet<>(words1);
        union.addAll(words2);

        double similarity = (double) intersection.size() / union.size();
        return similarity >= 0.5; // 50% word overlap = same issue
    }

    private List<Map<String, String>> deduplicate(List<Map<String, String>> findings) {
        List<Map<String, String>> result = new ArrayList<>();

        for (Map<String, String> candidate : findings) {
            boolean isDuplicate = false;
            for (int i = 0; i < result.size(); i++) {
                Map<String, String> existing = result.get(i);
                boolean sameLine = candidate.getOrDefault("line", "null")
                        .equals(existing.getOrDefault("line", "null"));
                boolean similarIssue = isSimilar(
                        candidate.getOrDefault("issue", ""),
                        existing.getOrDefault("issue", "")
                );
                if (sameLine && similarIssue) {
                    // Keep higher confidence
                    double existingConf = Double.parseDouble(existing.getOrDefault("confidence", "0"));
                    double candidateConf = Double.parseDouble(candidate.getOrDefault("confidence", "0"));
                    if (candidateConf > existingConf) {
                        result.set(i, candidate);
                    }
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                result.add(candidate);
            }
        }
        return result;
    }

    public void saveAnalysisAsComments(Long prId, Long codeFileId,
                                       List<Map<String, String>> findings, String authToken) {
        if (findings.isEmpty()) return;

        log.info("Saving {} AI findings as comments on PR {}", findings.size(), prId);
        for (Map<String, String> finding : findings) {
            String content = formatFindingAsComment(finding);
            Integer lineNumber = parseLineNumber(finding.get("line"));
            fileServiceClient.saveComment(prId, codeFileId, content, lineNumber, authToken);
        }
    }

    private String formatFindingAsComment(Map<String, String> finding) {
        return String.format("[AI-%s] %s (Severity: %s, Confidence: %s)\nSuggestion: %s",
                finding.getOrDefault("strategy", "ANALYSIS"),
                finding.getOrDefault("issue", ""),
                finding.getOrDefault("severity", ""),
                finding.getOrDefault("confidence", ""),
                finding.getOrDefault("suggestion", "")
        );
    }

    private Integer parseLineNumber(String line) {
        try {
            if (line == null || line.equals("null")) return null;
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
