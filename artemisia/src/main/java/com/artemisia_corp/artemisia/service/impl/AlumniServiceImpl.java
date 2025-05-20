package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Alumni;
import com.artemisia_corp.artemisia.entity.dto.alumni.AlumniRequestDto;
import com.artemisia_corp.artemisia.entity.dto.alumni.AlumniResponseDto;
import com.artemisia_corp.artemisia.repository.AlumniRepository;
import com.artemisia_corp.artemisia.service.AlumniService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlumniServiceImpl implements AlumniService {
    @Autowired
    private AlumniRepository alumniRepository;

    @Override
    public List<AlumniResponseDto> getAllAlumni() {
        return this.alumniRepository.findAllAlumni();
    }

    @Override
    public void postAlumni(AlumniRequestDto alumni) {
        this.alumniRepository.save(Alumni.builder()
                .ci(alumni.getCi())
                .complete_name(alumni.getName())
                .build());
    }
}
