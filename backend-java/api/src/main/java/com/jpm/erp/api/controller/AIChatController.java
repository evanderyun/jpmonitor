package com.jpm.erp.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class AIChatController {

    private static final Logger log = LoggerFactory.getLogger(AIChatController.class);
    private static final int TIMEOUT_MS = 180000; // 3 min for AI thinking

    @Value("${app.hestia-api:http://host.docker.internal:8765/chat}")
    private String hestiaApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public AIChatController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "").trim();
        if (message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message required"));
        }

        log.info("Hestia request: {}", message.substring(0, Math.min(100, message.length())));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String jsonBody = "{\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> hestiaResp = restTemplate.exchange(hestiaApiUrl, HttpMethod.POST, entity, String.class);

            if (hestiaResp.getStatusCode() == HttpStatus.OK && hestiaResp.getBody() != null) {
                JsonNode root = mapper.readTree(hestiaResp.getBody());
                JsonNode replyNode = root.get("reply");
                String reply = replyNode != null ? replyNode.asText() : null;

                if (reply != null && !reply.isEmpty()) {
                    return ResponseEntity.ok(Map.of("reply", reply));
                }
            }

            log.warn("Hestia unexpected: {}", hestiaResp.getStatusCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Hestia AI returned unexpected response"));

        } catch (ResourceAccessException e) {
            log.error("Hestia timeout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(Map.of("error", "Hestia sedang memproses. Tunggu beberapa detik lalu coba lagi."));
        } catch (Exception e) {
            log.error("Hestia error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Gagal menghubungi Hestia AI"));
        }
    }
}
