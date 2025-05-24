package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Address;
import com.artemisia_corp.artemisia.entity.Users;
import com.artemisia_corp.artemisia.entity.dto.address.*;
import com.artemisia_corp.artemisia.repository.AddressRepository;
import com.artemisia_corp.artemisia.repository.UsersRepository;
import com.artemisia_corp.artemisia.service.AddressService;
import com.artemisia_corp.artemisia.service.LogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private LogsService logsService;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDto> listAll() {
        return addressRepository.findAllAddresses();
    }

    @Override
    @Transactional
    public void save(AddressRequestDto addressDto) {
        Users user = usersRepository.findById(addressDto.getUser_id())
                .orElseThrow(() -> {
                    logsService.info("User not found with Id:" + addressDto.getUser_id());
                    log.warn("User not found with Id:" + addressDto.getUser_id());
                    return new RuntimeException("User not found");
                });

        Address address = Address.builder()
                .direction(addressDto.getDirection())
                .user(user)
                .build();

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void delete(AddressDeleteDto addressDto) {
        if (!addressRepository.existsById(addressDto.getAddressId())) return;

        addressRepository.deleteById(addressDto.getAddressId());
    }

    @Override
    @Transactional
    public void update(AddressUpdateDto addressDto) {
        Address address = addressRepository.findById(addressDto.getAddressId())
                .orElse(null);

        // Actualizar dirección
        if (addressDto.getDirection() != null && !addressDto.getDirection().isBlank()) {
            address.setDirection(addressDto.getDirection());
        }

        // Actualizar usuario si es necesario
        if (addressDto.getUser_id() != null) {
            Users user = usersRepository.findById(addressDto.getUser_id())
                    .orElse(null);
            address.setUser(user);
        }

        addressRepository.save(address);
    }
}