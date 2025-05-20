package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.Company;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyRequestDto;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyUpdateDto;
import com.artemisia_corp.artemisia.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping()
    public ResponseEntity<List<Company>> list() {
        try {
            return ResponseEntity.ok(companyService.listAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<List<Void>> save(@RequestBody CompanyRequestDto dto) {
        try {
            companyService.save(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<Void>> deleate(@RequestBody CompanyDeleteDto dto) {
        try {
            companyService.delete(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<List<Void>> update(@RequestBody CompanyUpdateDto dto) {
        try {
            companyService.update(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
