package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Address;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto(" +
            "a.addressId, a.direction, a.user.id) " +
            "FROM Address a")
    List<AddressResponseDto> findAllAddresses();

    List<Address> findByUser_Id(Long userId);
}