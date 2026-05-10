package com.codereview.platform.listener;

import com.codereview.platform.client.FileServiceClient;
import com.codereview.platform.event.PRCreatedEvent;
import com.codereview.platform.service.CodeAnalysisService;
import com.codereview.platform.service.SystemTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisListener {

    private final CodeAnalysisService codeAnalysisService;
    private final FileServiceClient fileServiceClient;
    private final SystemTokenService systemTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "pr-created", groupId = "review-service-group")
    public void onPRCreated(String message) {
        try {
            log.info("[AI-TRIGGER] Received pr-created event: {}", message);
            PRCreatedEvent event = objectMapper.readValue(message, PRCreatedEvent.class);

            log.info("[AI-TRIGGER] Starting auto-analysis for PR {} in repo {}",
                    event.getPrId(), event.getRepositoryId());

            // We need a system token to call file-service
            // For now we'll skip auth — we'll handle this next
            String systemToken = getSystemToken();
            if (systemToken == null) {
                log.warn("[AI-TRIGGER] Could not obtain system token, skipping analysis for PR {}", event.getPrId());
                return;
            }

            // Fetch existing repo files for codebase context
            Map<String, String> repoFiles = fetchRepoFiles(event.getRepositoryId(), systemToken);
            if (repoFiles.isEmpty()) {
                log.warn("[AI-TRIGGER] No files found in repo {}, skipping PR {}",
                        event.getRepositoryId(), event.getPrId());
                return;
            }


            log.info("[AI-TRIGGER] Analyzing {} files for PR {}", repoFiles.size(), event.getPrId());

            for (Map.Entry<String, String> file : repoFiles.entrySet()) {
                String language = detectLanguage(file.getKey());
                List<Map<String, String>> findings = codeAnalysisService.analyze(
                        file.getValue(), language, "ALL", repoFiles);

                if (!findings.isEmpty()) {
                    codeAnalysisService.saveAnalysisAsComments(
                            event.getPrId(), null, findings, systemToken);
                    log.info("[AI-TRIGGER] Saved {} findings for file {} on PR {}",
                            findings.size(), file.getKey(), event.getPrId());
                }
            }
            log.info("[AI-TRIGGER] Completed auto-analysis for PR {}", event.getPrId());
        } catch (Exception e) {
            log.error("[AI-TRIGGER] Failed to process pr-created event: {}", e.getMessage());
        }
    }

    private Map<String, String> fetchRepoFiles(Long repoId, String token) {
        Map<String, String> files = new HashMap<>();
        try {
            List<Map<String, Object>> fileList = fileServiceClient.getFilesForRepo(repoId, token);
            log.info("[AI-TRIGGER] File list size: {}", fileList.size());
            for (Map<String, Object> file : fileList) {
                log.info("[AI-TRIGGER] Processing file: {}", file);
                Long fileId = Long.valueOf(file.get("id").toString());
                String filename = (String) file.get("fileName");
                log.info("[AI-TRIGGER] fileId={}, filename={}", fileId, filename);
                Map<String, String> content = fileServiceClient.downloadFile(repoId, fileId, token);
                log.info("[AI-TRIGGER] Download result empty: {}", content.isEmpty());
                if (!content.isEmpty()) {
                    files.put(filename, content.get("content"));
                }
            }
        } catch (Exception e) {
            log.error("[AI-TRIGGER] Error fetching repo files: {}", e.getMessage());
        }
        return files;
    }

    private Map<String, String> fetchPRFiles(Long prId, String token) {
        // For now return empty — we'll implement this properly
        // PR files require fetching PR details then its associated files
        return new HashMap<>();
    }

    private String detectLanguage(String filename) {
        if (filename.endsWith(".java")) return "java";
        if (filename.endsWith(".py")) return "python";
        if (filename.endsWith(".js")) return "javascript";
        if (filename.endsWith(".ts")) return "typescript";
        if (filename.endsWith(".go")) return "go";
        return "java"; // default
    }

    private String getSystemToken() {
        return systemTokenService.getToken();
    }
}