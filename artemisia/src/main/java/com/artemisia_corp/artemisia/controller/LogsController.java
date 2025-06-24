package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto;
import com.artemisia_corp.artemisia.service.LogsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/logs")
@Tag(name = "Logs", description = "Endpoints for logs")
public class LogsController {
    @Autowired
    private LogsService logsService;

    @Operation(summary = "Get all logs", description = "Get all system logs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "logs gotten successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping
    public ResponseEntity<List<LogsResponseDto>> getAllLogs() {
        return ResponseEntity.ok(logsService.getAllLogs());
    }
}