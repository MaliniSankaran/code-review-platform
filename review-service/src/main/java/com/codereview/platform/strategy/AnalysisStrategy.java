package com.codereview.platform.strategy;

import java.util.List;
import java.util.Map;

public interface AnalysisStrategy {

    String getStrategyName();

    List<Map<String, String>> analyze(String code, String language, Map<String, String> existingFiles);
}
