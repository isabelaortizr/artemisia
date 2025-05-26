package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Address;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.address.AddressRequestDto;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import com.artemisia_corp.artemisia.repository.AddressRepository;
import com.artemisia_corp.artemisia.repository.UserRepository;
import com.artemisia_corp.artemisia.service.AddressService;
import com.artemisia_corp.artemisia.service.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final LogsService logsService;

    @Override
    public List<AddressResponseDto> getAllAddresses() {
        logsService.info("Fetching all addresses");
        return addressRepository.findAllAddresses();
    }

    @Override
    public AddressResponseDto getAddressById(Long id) {
        logsService.info("Fetching address with ID: " + id);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Address not found with ID: " + id);
                    throw new RuntimeException("Address not found");
                });
        return convertToDto(address);
    }

    @Override
    public AddressResponseDto createAddress(AddressRequestDto addressDto) {
        User user = userRepository.findById(addressDto.getUserId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + addressDto.getUserId());
                    throw new RuntimeException("User not found");
                });

        Address address = Address.builder()
                .direction(addressDto.getDirection())
                .user(user)
                .build();

        Address savedAddress = addressRepository.save(address);
        logsService.info("Address created with ID: " + savedAddress.getAddressId());
        return convertToDto(savedAddress);
    }

    @Override
    public AddressResponseDto updateAddress(Long id, AddressRequestDto addressDto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Address not found with ID: " + id);
                    throw new RuntimeException("Address not found");
                });

        User user = userRepository.findById(addressDto.getUserId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + addressDto.getUserId());
                    throw new RuntimeException("User not found");
                });

        address.setDirection(addressDto.getDirection());
        address.setUser(user);

        Address updatedAddress = addressRepository.save(address);
        logsService.info("Address updated with ID: " + updatedAddress.getAddressId());
        return convertToDto(updatedAddress);
    }

    @Override
    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            logsService.error("Address not found with ID: " + id);
            throw new RuntimeException("Address not found");
        }
        addressRepository.deleteById(id);
        logsService.info("Address deleted with ID: " + id);
    }

    @Override
    public List<AddressResponseDto> getAddressesByUser(Long userId) {
        logsService.info("Fetching addresses for user ID: " + userId);
        return addressRepository.findByUser_Id(userId);
    }

    private AddressResponseDto convertToDto(Address address) {
        return AddressResponseDto.builder()
                .addressId(address.getAddressId())
                .direction(address.getDirection())
                .userId(address.getUser().getId())
                .build();
    }
}