package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Alumni;
import com.artemisia_corp.artemisia.entity.dto.alumni.AlumniResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.alumni.AlumniResponseDto(a.id, a.complete_name, a.ci) " +
            "FROM Alumni a")
    List<AlumniResponseDto> findAllAlumni();

}
