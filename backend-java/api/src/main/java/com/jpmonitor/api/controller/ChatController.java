package com.jpmonitor.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmonitor.api.dto.ChatRequest;
import com.jpmonitor.api.dto.ChatResponse;
import com.jpmonitor.domains.core.entity.User;
import com.jpmonitor.domains.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Value("${hermes.api.url:http://localhost:8642}")
    private String hermesApiUrl;

    @Value("${hermes.api.key:hermes-jpmonitor-dev}")
    private String hermesApiKey;

    public ChatController(RestTemplate restTemplate, ObjectMapper objectMapper, UserService userService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        String message = request.message() != null ? request.message().trim() : "";
        if (message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        String username = userDetails.getUsername();
        log.info("Chat request from user '{}': {}", username,
                message.substring(0, Math.min(100, message.length())));

        try {
            String systemPrompt = buildSystemPrompt(username);
            String reply = callHermesApi(systemPrompt, message, request.stream());

            return ResponseEntity.ok(new ChatResponse(reply, request.conversationId()));

        } catch (ResourceAccessException e) {
            log.error("Hermes API unreachable: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "AI service is currently unavailable. Please try again later."));
        } catch (Exception e) {
            log.error("Chat error for user '{}': {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process chat request"));
        }
    }

    private String buildSystemPrompt(String username) {
        User user = userService.findByUsername(username);
        String roleName = user.getRole() != null ? user.getRole().getName() : "Unknown";
        return String.format(
                """
                You are Hermes, an intelligent AI assistant integrated into the JP Monitor ERP system.
                
                Current user context:
                - Name: %s
                - Username: %s
                - Role: %s
                
                You help the user with ERP operations, data analysis, reporting, and general assistance.
                Keep responses concise and relevant to the mining/ERP context.
                """,
                user.getFullName(), user.getUsername(), roleName
        ).strip();
    }

    private String callHermesApi(String systemPrompt, String userMessage, boolean stream) throws Exception {
        String url = hermesApiUrl + "/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", "hermes",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "stream", stream
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(hermesApiKey);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.warn("Hermes API returned unexpected status: {}", response.getStatusCode());
            throw new RuntimeException("Hermes API returned status: " + response.getStatusCode());
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && !choices.isEmpty()) {
            JsonNode messageNode = choices.get(0).get("message");
            if (messageNode != null) {
                JsonNode content = messageNode.get("content");
                if (content != null && !content.isNull()) {
                    return content.asText();
                }
            }
        }

        log.warn("Could not extract reply from Hermes response");
        return "I'm sorry, I couldn't generate a response at this time.";
    }
}
