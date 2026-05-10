package com.codereview.platform.service;

import java.util.Map;

public interface AIService {
    String analyze ( String code, String language, String analysisType);
    String analyzeWithContext(String code, String language, String analysisType, Map<String, String> existingFiles);

}
