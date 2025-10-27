package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Logs;
import com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto;
import com.artemisia_corp.artemisia.repository.LogerRespository;
import com.artemisia_corp.artemisia.service.LogsService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class LogsServiceImpl implements LogsService {
    private LogerRespository logerRespository;

    @Override
    public List<LogsResponseDto> getAllLogs() {
        return logerRespository.findAllLogs();
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void info(String message) {
        Logs logs = Logs.builder()
                .level("INFO")
                .message(message)
                .date(new Date())
                .build();
        logerRespository.save(logs);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void warning(String message) {
        Logs logs = Logs.builder()
                .level("WARNING")
                .message(message)
                .date(new Date())
                .build();
        logerRespository.save(logs);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void error(String message) {
        Logs logs = Logs.builder()
                .level("ERROR")
                .message(message)
                .date(new Date())
                .build();
        logerRespository.save(logs);
    }
}
