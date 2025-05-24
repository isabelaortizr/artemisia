package com.artemisia_corp.artemisia.entity.dto.logs;

import com.artemisia_corp.artemisia.entity.Logs;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LogsResponseDto {
    private Long id;
    private String level;
    private String message;
    private Date timestamp;

    public LogsResponseDto(Logs logs) {
        this.id = logs.getId();
        this.level = logs.getLevel();
        this.message = logs.getMessage();
        this.timestamp = logs.getDate();
    }
}
