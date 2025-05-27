package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto;
import com.artemisia_corp.artemisia.service.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/logs")
public class LogsController {

    @Autowired
    private LogsService logsService;

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