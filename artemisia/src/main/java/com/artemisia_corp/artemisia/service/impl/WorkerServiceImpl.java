package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Company;
import com.artemisia_corp.artemisia.entity.Product;
import com.artemisia_corp.artemisia.entity.Worker;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerRequestDto;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerUpdateDto;
import com.artemisia_corp.artemisia.repository.CompanyRepository;
import com.artemisia_corp.artemisia.repository.WorkerRepository;
import com.artemisia_corp.artemisia.service.WorkerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class WorkerServiceImpl implements WorkerService {
    private final WorkerRepository workerRepository;
    private CompanyRepository companyRepository;

    @Override
    public List<Worker> listAll() {
        return workerRepository.findAll();
    }

    @Override
    public void save(WorkerRequestDto worker) {
        Company company = companyRepository.findById(worker.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + worker.getCompanyId()));
        this.workerRepository.save(Worker.builder()
                .name(worker.getName())
                .occupation(worker.getOccupation())
                .state(worker.getState())
                .salary(worker.getSalary())
                .company(company)
                .build()
        );
    }

    @Override
    public void delete(WorkerDeleteDto worker) {
        workerRepository.deleteById(worker.getId());
    }

    @Override
    public void update(WorkerUpdateDto worker) {
        Worker existingWorker = workerRepository.findById(worker.getId())
                .orElseThrow(() -> new RuntimeException("Company with ID " + worker.getId() + " not found"));

        Company company = companyRepository.findById(worker.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + worker.getCompanyId()));

        existingWorker.setName(worker.getName());
        existingWorker.setOccupation(worker.getOccupation());
        existingWorker.setState(worker.getState());
        existingWorker.setSalary(worker.getSalary());
        existingWorker.setCompany(company);
        this.workerRepository.save(existingWorker);
    }
}
