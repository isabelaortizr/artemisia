package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.Worker;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerRequestDto;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerUpdateDto;

import java.util.List;

public interface WorkerService {
    List<Worker> listAll();
    void save(WorkerRequestDto worker);
    void delete(WorkerDeleteDto worker);
    void update(WorkerUpdateDto worker);
}
