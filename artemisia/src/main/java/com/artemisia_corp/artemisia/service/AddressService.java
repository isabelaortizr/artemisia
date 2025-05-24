package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.dto.address.*;

import java.util.List;

public interface AddressService {
    List<AddressResponseDto> listAll();
    void save(AddressRequestDto address);
    void delete(AddressDeleteDto address);
    void update(AddressUpdateDto address);
}