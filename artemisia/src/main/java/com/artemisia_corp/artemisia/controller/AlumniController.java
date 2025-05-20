package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.Client;
import com.artemisia_corp.artemisia.entity.dto.alumni.AlumniRequestDto;
import com.artemisia_corp.artemisia.entity.dto.alumni.AlumniResponseDto;
import com.artemisia_corp.artemisia.entity.dto.client.ClientRequestDto;
import com.artemisia_corp.artemisia.service.AlumniService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor

@RestController
@RequestMapping("/api/v1/alumni")
public class AlumniController {
    private final AlumniService alumniService;

    @GetMapping()
    public ResponseEntity<List<AlumniResponseDto>> list() {
        try {
            return ResponseEntity.ok(alumniService.getAllAlumni());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<List<Void>> save(@RequestBody AlumniRequestDto dto) {
        try {
            alumniService.postAlumni(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
