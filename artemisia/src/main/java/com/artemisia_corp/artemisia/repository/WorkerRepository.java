package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
}
