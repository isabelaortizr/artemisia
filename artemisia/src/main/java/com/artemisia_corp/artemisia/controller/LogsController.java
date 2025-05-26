package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto;
import com.artemisia_corp.artemisia.service.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogsController {

    private final LogsService logsService;

    @GetMapping
    public ResponseEntity<List<LogsResponseDto>> getAllLogs() {
        return ResponseEntity.ok(logsService.getAllLogs());
    }

    @PostMapping("/info")
    public ResponseEntity<Void> logInfo(@RequestBody String message) {
        logsService.info(message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/warning")
    public ResponseEntity<Void> logWarning(@RequestBody String message) {
        logsService.warning(message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/error")
    public ResponseEntity<Void> logError(@RequestBody String message) {
        logsService.error(message);
        return ResponseEntity.ok().build();
    }
}