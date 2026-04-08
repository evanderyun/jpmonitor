package com.jpm.erp.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class AIChatController {

    private static final Logger log = LoggerFactory.getLogger(AIChatController.class);

    @Value("${app.hestia-api:http://host.docker.internal:8765/chat}")
    private String hestiaApiUrl;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message required"));
        }

        try {
            log.info("Hestia request: {}", message.substring(0, Math.min(100, message.length())));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String jsonBody = "{\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> hestiaResp = restTemplate.exchange(hestiaApiUrl, HttpMethod.POST, entity, String.class);

            if (hestiaResp.getStatusCode() == HttpStatus.OK && hestiaResp.getBody() != null) {
                String rawBody = hestiaResp.getBody();
                int replyStart = rawBody.indexOf("\"reply\":");
                if (replyStart >= 0) {
                    int valueStart = rawBody.indexOf("\"", replyStart + 8) + 1;
                    int valueEnd = rawBody.lastIndexOf("\"");
                    String reply = rawBody.substring(valueStart, valueEnd)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"");
                    return ResponseEntity.ok(Map.of("reply", reply));
                }
            }

            log.warn("Hestia unexpected response: {}", hestiaResp.getStatusCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Hestia AI returned an unexpected response"));

        } catch (RestClientException e) {
            log.error("Hestia API error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Gagal menghubungi Hestia AI"));
        } catch (Exception e) {
            log.error("Chat error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Terjadi kesalahan: " + e.getMessage()));
        }
    }
}
