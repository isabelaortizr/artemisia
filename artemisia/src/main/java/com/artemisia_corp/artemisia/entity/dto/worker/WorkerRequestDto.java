package com.artemisia_corp.artemisia.entity.dto.worker;

import com.artemisia_corp.artemisia.entity.enums.WorkerStateEntity;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WorkerRequestDto {
    String name;
    String occupation;
    WorkerStateEntity state;
    double salary;
    long companyId;
}
