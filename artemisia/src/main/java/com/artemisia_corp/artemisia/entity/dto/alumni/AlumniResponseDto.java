package com.artemisia_corp.artemisia.entity.dto.alumni;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlumniResponseDto {
    private long id;
    private String name;
    private int ci;

    public AlumniResponseDto(long id, String name, int ci) {
        this.id = id;
        this.name = name;
        this.ci = ci;
    }
}
