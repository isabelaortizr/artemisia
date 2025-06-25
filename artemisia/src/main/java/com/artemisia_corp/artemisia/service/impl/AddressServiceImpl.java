package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Address;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.address.AddressRequestDto;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import com.artemisia_corp.artemisia.exception.ClientNotFoundException;
import com.artemisia_corp.artemisia.exception.IncompleteAddressException;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.AddressRepository;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.AddressService;
import com.artemisia_corp.artemisia.service.LogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogsService logsService;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDto> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();

        if (addresses.isEmpty()) {
            logsService.error("No addresses found: There are no addresses in the system.");
            throw new NotDataFoundException("No addresses found: There are no addresses in the system.");
        }

        return addresses.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDto getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Address not found: The address with ID " + id + " does not exist.");
                    logsService.error("Address not found: The address with ID " + id + " does not exist.");
                    return new NotDataFoundException("Address not found: The address with ID " +
                            id + " does not exist.");
                });

        return convertToDto(address);
    }

    @Override
    public AddressResponseDto createAddress(AddressRequestDto addressDto) {
        User user = userRepository.findById(addressDto.getUserId())
                .orElseThrow(() -> {
                    log.error("Client not found: The user with ID " +
                                    addressDto.getUserId() + " does not exist.");
                    logsService.error("Client not found: The user with ID " +
                            addressDto.getUserId() + " does not exist.");
                    return new ClientNotFoundException("Client not found: The user with ID " +
                            addressDto.getUserId() + " does not exist.");
                });

        if (addressDto.getDirection() == null || addressDto.getDirection().trim().isEmpty()) {
            log.error("Address is incomplete: 'Direction' is missing.");
            logsService.error("Address is incomplete: 'Direction' is missing.");
            throw new IncompleteAddressException("Address is incomplete: 'Direction' is missing.");
        }

        Address address = new Address();
        address.setDirection(addressDto.getDirection());
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);

        return convertToDto(savedAddress);
    }

    @Override
    public AddressResponseDto updateAddress(Long id, AddressRequestDto addressDto) {
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Address not found: The address with ID " + id + " does not exist.");
                    logsService.error("Address not found: The address with ID " + id + " does not exist.");
                    return new NotDataFoundException("Address not found: The address with ID " + id + " does not exist.");
                });

        User user = userRepository.findById(addressDto.getUserId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found: The user with ID " + addressDto.getUserId() + " does not exist."));

        if (addressDto.getDirection() == null || addressDto.getDirection().trim().isEmpty()) {
            throw new IncompleteAddressException("Address is incomplete: 'Direction' is missing.");
        }

        existingAddress.setDirection(addressDto.getDirection());
        existingAddress.setUser(user);

        Address updatedAddress = addressRepository.save(existingAddress);

        return convertToDto(updatedAddress);
    }

    @Override
    public void deleteAddress(Long id) {
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new NotDataFoundException("Address not found: The address with ID " + id + " does not exist."));

        addressRepository.delete(existingAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressResponseDto> getAddressesByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Client not found: The user with ID " + userId + " does not exist.");
                    logsService.error("Client not found: The user with ID " + userId + " does not exist.");
                    return new ClientNotFoundException("Client not found: The user with ID " + userId + " does not exist.");
                });

        Page<Address> addressPage = addressRepository.findByUser(user, pageable);

        if (addressPage.isEmpty()) {
            log.error("No addresses found: The user with ID " + userId + " does not have any addresses.");
            logsService.error("No addresses found: The user with ID " + userId + " does not have any addresses.");
            throw new NotDataFoundException("No addresses found: The user with ID " + userId + " does not have any addresses.");
        }

        return addressPage.map(this::convertToDto);
    }

    private AddressResponseDto convertToDto(Address address) {
        return AddressResponseDto.builder()
                .addressId(address.getId())
                .direction(address.getDirection())
                .userId(address.getUser().getId())
                .build();
    }
}