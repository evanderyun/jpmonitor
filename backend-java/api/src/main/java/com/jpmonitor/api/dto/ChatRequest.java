package com.jpmonitor.api.dto;

public record ChatRequest(String message, boolean stream, String conversationId) {
}
