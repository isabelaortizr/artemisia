package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto;

import java.util.List;

public interface LogsService {
    List<LogsResponseDto> getAllLogs();
    void info(String message);
    void warning(String message);
    void error(String message);
}
