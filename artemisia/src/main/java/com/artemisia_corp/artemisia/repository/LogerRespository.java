package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Logs;
import com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogerRespository extends JpaRepository<Logs, Long> {
    List<LogsResponseDto> findAllLogs();
}
