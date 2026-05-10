package com.codereview.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SystemTokenService {

    @Value("${ai.service.username}")
    private String username;

    @Value("${ai.service.password}")
    private String password;

    @Value("${ai.service.auth-url}")
    private String authUrl;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;
    private long tokenExpiry;

    @PostConstruct
    public void init() {
        try {
            login();
            log.info("[SYSTEM-TOKEN] AI service account token obtained successfully");
        } catch (Exception e) {
            log.error("[SYSTEM-TOKEN] Failed to obtain token on startup: {}", e.getMessage());
        }
    }

    public String getToken() {
        if (token == null || isExpired()) {
            try {
                login();
            } catch (Exception e) {
                log.error("[SYSTEM-TOKEN] Failed to refresh token: {}", e.getMessage());
                return null;
            }
        }
        return "Bearer " + token;
    }

    private void login() throws Exception {
        Map<String, String> body = Map.of(
                "email", username,
                "password", password
        );

        String json = objectMapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(authUrl)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Login failed with status: " + response.code());
            }
            Map<String, Object> responseMap = objectMapper.readValue(
                    response.body().string(), Map.class);
            this.token = (String) responseMap.get("accessToken");
            log.info("[SYSTEM-TOKEN] Token obtained for user, token prefix: {}",
                    this.token != null ? this.token.substring(0, 20) : "null");
            // JWT tokens typically expire in 24h, refresh after 23h
            this.tokenExpiry = System.currentTimeMillis() + (23 * 60 * 60 * 1000);
            log.info("[SYSTEM-TOKEN] Token refreshed successfully");
        }
    }

    private boolean isExpired() {
        return token == null || token.isEmpty() || System.currentTimeMillis() > tokenExpiry;
    }
}