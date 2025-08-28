package com.artemisia_corp.artemisia.entity.dto.admin_dashboard;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class NewUsersReportDto {
    private Date startDate;
    private Date endDate;
    private long newUsersCount;
}
