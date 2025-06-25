package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.Address;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import com.artemisia_corp.artemisia.entity.enums.AddressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT new com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto(a)" +
            "FROM Address a")
    List<AddressResponseDto> findAllAddresses();

    Page<Address> findByUserAndStatus(User user, AddressStatus status, Pageable pageable);

    @Query("SELECT a " +
            "FROM Address a " +
            "WHERE a.user.id =:user_id AND a.status = 'ACTIVE' " +
            "ORDER BY a.id DESC LIMIT 1")
    Address findLastAddressByUser_Id(@Param("user_id") Long userId);

    Optional<Address> findAddressByIdAndUser_Id(Long id, Long userId);
}
