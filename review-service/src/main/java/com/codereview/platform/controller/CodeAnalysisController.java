package com.codereview.platform.controller;

import com.codereview.platform.service.CodeAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class CodeAnalysisController {

    private final CodeAnalysisService codeAnalysisService;

    @PostMapping
    public ResponseEntity<List<Map<String, String>>> analyzeCode(
            @RequestParam String language,
            @RequestParam(defaultValue = "ALL") String analysisType,
            @RequestParam(required = false) Long prId,
            @RequestParam(required = false) Long codeFileId,
            @RequestBody String code,
            @RequestHeader(value = "Authorization", required = false) String authToken) {

        log.info("Analysis request: type={}, language={}, prId={}", analysisType, language, prId);
        List<Map<String, String>> results = codeAnalysisService.analyze(code, language, analysisType);

        if (prId != null && authToken != null) {
            codeAnalysisService.saveAnalysisAsComments(prId, codeFileId, results, authToken);
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getAvailableTypes() {
        return ResponseEntity.ok(List.of("SECURITY", "PERFORMANCE", "STYLE", "ALL"));
    }

}
