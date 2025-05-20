package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.Company;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyRequestDto;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyUpdateDto;

import java.util.List;

public interface CompanyService {
    List<Company> listAll();
    void save(CompanyRequestDto company);
    void delete(CompanyDeleteDto company);
    void update(CompanyUpdateDto company);
}
