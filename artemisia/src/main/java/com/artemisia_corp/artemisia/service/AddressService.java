package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.address.AddressRequestDto;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AddressService {
    List<AddressResponseDto> getAllAddresses();
    AddressResponseDto getAddressById(Long id, String token);
    AddressResponseDto createAddress(AddressRequestDto addressDto, String token);
    AddressResponseDto updateAddress(Long id, AddressRequestDto addressDto, String token);
    void deleteAddress(Long id, String token);
    Page<AddressResponseDto> getAddressesByUser(Long userId, Pageable pageable, String token);
}
