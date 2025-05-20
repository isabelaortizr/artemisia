package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.Alumni;
import com.artemisia_corp.artemisia.entity.dto.alumni.AlumniRequestDto;
import com.artemisia_corp.artemisia.entity.dto.alumni.AlumniResponseDto;

import java.util.List;

public interface AlumniService {
    public List<AlumniResponseDto> getAllAlumni();
    public void postAlumni(AlumniRequestDto alumni);
}
