package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}
