package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
