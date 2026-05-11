package com.jpmonitor.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @GetMapping
    public ResponseEntity<List<Object>> getLogs() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}