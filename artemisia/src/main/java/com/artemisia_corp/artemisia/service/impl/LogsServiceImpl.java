package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Logs;
import com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto;
import com.artemisia_corp.artemisia.repository.LogerRespository;
import com.artemisia_corp.artemisia.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class LogsServiceImpl implements LogsService {
    @Autowired
    private LogerRespository logerRespository;

    @Override

    public List<LogsResponseDto> getLogs() {
        return logerRespository.findAllLogs();
    }

    @Override
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void warning(String message) {
        Logs logs = Logs.builder()
                .level("WARING")
                .message(message)
                .date(new Date())
                .build();
        logerRespository.save(logs);
    }

    @Override
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
