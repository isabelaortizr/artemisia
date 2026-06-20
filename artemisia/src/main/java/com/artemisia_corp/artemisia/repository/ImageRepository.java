package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("SELECT i.cloudinaryUrl FROM Image i WHERE i.product.id =:product_id ORDER BY i.id asc limit 1")
    String findLatestCloudinaryUrlByProductId(@Param("product_id") Long productId);

    @Query("SELECT i FROM Image i WHERE i.product.id =:product_id ORDER BY i.id asc limit 1")
    Image findFirstByProductId(@Param("product_id") Long productId);
}
