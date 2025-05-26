package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.address.AddressRequestDto;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;

import java.util.List;

public interface AddressService {
    List<AddressResponseDto> getAllAddresses();
    AddressResponseDto getAddressById(Long id);
    AddressResponseDto createAddress(AddressRequestDto addressDto);
    AddressResponseDto updateAddress(Long id, AddressRequestDto addressDto);
    void deleteAddress(Long id);
    List<AddressResponseDto> getAddressesByUser(Long userId);
}