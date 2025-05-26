package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Logs;
import com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LogerRespository extends JpaRepository<Logs, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.logs.LogsResponseDto(" +
            "l.id, l.level, l.message, l.date) " +
            "FROM Logs l")

    List<LogsResponseDto> findAllLogs();
}