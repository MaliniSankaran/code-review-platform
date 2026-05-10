package com.codereview.platform.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AnalysisContext {

    private AnalysisStrategy strategy;
    private Map<String, String> existingFiles;

    //Swap active strategy at runtime
    public void setStrategy(AnalysisStrategy strategy) {
        log.info("Switching analysis strategy to {}", strategy.getStrategyName());
        this.strategy = strategy;
    }

    public void setExistingFiles(Map<String, String> existingFiles) {
        this.existingFiles = existingFiles;
    }

    public void clearExistingFiles() {
        this.existingFiles = null;
    }

    public String getActiveStrategyName() {
        if (strategy == null) return "NONE";
        return strategy.getStrategyName();
    }

    // Callers always come here — never call strategy.analyze() directly
    public List<Map<String, String>> executeAnalysis(String code, String language) {
        if (strategy == null) {
            throw new IllegalStateException("No analysis strategy set. Call setStrategy() first.");
        }
        log.info("Executing {} analysis on {} code (context: {})",
                strategy.getStrategyName(), language,
                existingFiles != null ? existingFiles.size() + " files" : "none");
        return strategy.analyze(code, language, existingFiles);
    }
}
