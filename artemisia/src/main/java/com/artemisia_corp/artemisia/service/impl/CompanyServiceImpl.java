package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Company;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyRequestDto;
import com.artemisia_corp.artemisia.entity.dto.company.CompanyUpdateDto;
import com.artemisia_corp.artemisia.repository.CompanyRepository;
import com.artemisia_corp.artemisia.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@AllArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService {

    /*
    Hay 3 maneras de inyectar datos, una donde se declara la variable como final y se utiliza un constructor para
    crear la instancia
    Otra es utilizando @autowired que abre lo que se llama apenas se inicia la aplicacion, haciendolo un singleton, ej:
    @Autowired
    private CompanyRepository companyRepository;

    Otra es la lazy que es como la segunda pero se instancia solo cuando se necesite, haciendo que inicie
    mas rapido como no inicia cada instancia desde el inicio, ej:
    @Autowired
    @Lazy
    private CompanyRepository companyRepository;
     */
    private final CompanyRepository companyRepository;

    @Override
    public List<Company> listAll() {
        return companyRepository.findAll();
    }

    @Override
    public void save(CompanyRequestDto company) {
        this.companyRepository.save(Company.builder()
                .name(company.getName())
                .nit(company.getNit())
                .state(company.getState())
                .build());
    }

    @Override
    public void delete(CompanyDeleteDto dto) {
        this.companyRepository.deleteById(dto.getId());
    }

    @Override
    public void update(CompanyUpdateDto company) {
        Company existingCompany = companyRepository.findById(company.getId())
                .orElseThrow(() -> new RuntimeException("Company with ID " + company.getId() + " not found"));

        existingCompany.setName(company.getName());
        existingCompany.setNit(company.getNit());
        existingCompany.setState(company.getState());

        companyRepository.save(existingCompany);
    }
}
