package com.codereview.platform.service;

import com.codereview.platform.strategy.AnalysisContext;
import com.codereview.platform.strategy.AnalysisStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeAnalysisService {

    private final List<AnalysisStrategy> strategies;
    private final AnalysisContext analysisContext;

    public List<Map<String, String>> analyze(String code, String language, String analysisType) {

        if ("ALL".equalsIgnoreCase(analysisType)) {
            return runAllStrategies(code, language);
        }
        return runSingleStrategy(code, language, analysisType);
    }

    private List<Map<String, String>> runAllStrategies(String code, String language) {
        List<Map<String, String>> allFindings = new ArrayList<>();
        for (AnalysisStrategy strategy : strategies) {
            analysisContext.setStrategy(strategy);
            allFindings.addAll(analysisContext.executeAnalysis(code, language));
        }
        return allFindings;
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
}
