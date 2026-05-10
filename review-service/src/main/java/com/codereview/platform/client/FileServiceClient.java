package com.codereview.platform.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FileServiceClient {

    @Value("${services.file-service.url:http://localhost:8082}")
    private String fileServiceUrl;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveComment(Long prId, Long codeFileId, String content, Integer lineNumber, String authToken) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("content", content);
            if (codeFileId != null) body.put("codeFileId", codeFileId);
            if (lineNumber != null) body.put("lineNumber", lineNumber);

            String json = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(fileServiceUrl + "/api/pulls/" + prId + "/comments")
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.get("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("Saved AI comment on PR {}", prId);
                } else {
                    log.error("Failed to save comment on PR {}: {}", prId, response.code());
                }
            }
        } catch (Exception e) {
            log.error("Error saving comment to file-service: {}", e.getMessage());
        }
    }

    public Map<String, String> downloadFile(Long repoId,Long fileId, String authToken) {
        try {
            Request request = new Request.Builder()
                    .url(fileServiceUrl + "/api/repositories/" + repoId + "/files/" + fileId + "/download")
                    .header("Authorization", authToken)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return Map.of("content", response.body().string());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching file from file-service: {}", e.getMessage());
        }
        return Map.of();
    }

    public List<Map<String, Object>> getFilesForRepo(Long repoId, String authToken) {
        try {
            Request request = new Request.Builder()
                    .url(fileServiceUrl + "/api/repositories/" + repoId + "/files")
                    .header("Authorization", authToken)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                log.info("getFilesForRepo response code: {}, body: {}", response.code(), responseBody);
                if (response.isSuccessful()) {
                    return objectMapper.readValue(responseBody, List.class);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching repo files from file-service: {}", e.getMessage());
        }
        return List.of();
    }
}