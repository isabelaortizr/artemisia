package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Address;
import com.artemisia_corp.artemisia.entity.User;
import com.artemisia_corp.artemisia.entity.dto.address.AddressRequestDto;
import com.artemisia_corp.artemisia.entity.dto.address.AddressResponseDto;
import com.artemisia_corp.artemisia.entity.enums.AddressStatus;
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
                            id + " does not exist" );
                });

        return convertToDto(address);
    }

    @Override
    public AddressResponseDto createAddress(AddressRequestDto addressDto) {
        validateMandatoryFields(addressDto);

        User user = userRepository.findById(addressDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = Address.builder()
                .recipientName(addressDto.getRecipientName())
                .recipientSurname(addressDto.getRecipientSurname())
                .country(addressDto.getCountry())
                .city(addressDto.getCity())
                .street(addressDto.getStreet())
                .houseNumber(addressDto.getHouseNumber())
                .extra(addressDto.getExtra())
                .status(AddressStatus.ACTIVE)
                .user(user)
                .build();

        Address savedAddress = addressRepository.save(address);

        return convertToDto(savedAddress);
    }

    @Override
    public AddressResponseDto updateAddress(Long id, AddressRequestDto addressDto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (addressDto.getRecipientName() != null && !addressDto.getRecipientName().isEmpty()) 
            address.setRecipientName(addressDto.getRecipientName());
        if (addressDto.getRecipientSurname() != null && !addressDto.getRecipientSurname().isEmpty())
            address.setRecipientSurname(addressDto.getRecipientSurname());
        if (addressDto.getCountry() != null && !addressDto.getCountry().isEmpty())
            address.setCountry(addressDto.getCountry());
        if (addressDto.getCity() != null && !addressDto.getCity().isEmpty())
            address.setCity(addressDto.getCity());
        if (addressDto.getStreet() != null && !addressDto.getStreet().isEmpty())
            address.setStreet(addressDto.getStreet());
        if (addressDto.getHouseNumber() != null && !addressDto.getHouseNumber().isEmpty())
            address.setHouseNumber(addressDto.getHouseNumber());
        address.setExtra(addressDto.getExtra());


        Address updatedAddress = addressRepository.save(address);

        return convertToDto(updatedAddress);
    }

    @Override
    public void deleteAddress(Long id) {
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new NotDataFoundException("Address not found: The address with ID " + id + " does not exist."));

        existingAddress.setStatus(AddressStatus.DELETED);
        addressRepository.save(existingAddress);
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

        Page<Address> addressPage = addressRepository.findByUserAndStatus(user, AddressStatus.ACTIVE, pageable);

        return addressPage.map(this::convertToDto);
    }

    private AddressResponseDto convertToDto(Address address) {
        return AddressResponseDto.builder()
                .addressId(address.getId())
                .recipientName(address.getRecipientName())
                .recipientSurname(address.getRecipientSurname())
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .houseNumber(address.getHouseNumber())
                .extra(address.getExtra())
                .userId(address.getUser().getId())
                .build();
    }

    private void validateMandatoryFields(AddressRequestDto addressDto) {
        log.info("Validating mandatory fields for address: " + addressDto);
        if (addressDto == null ||
                addressDto.getUserId() == null ||
                addressDto.getRecipientName() == null ||
                addressDto.getRecipientSurname() == null ||
                addressDto.getCountry() == null ||
                addressDto.getCity() == null ||
                addressDto.getStreet() == null ||
                addressDto.getHouseNumber() == null ||
                addressDto.getRecipientName().isEmpty() ||
                addressDto.getRecipientSurname().isEmpty() ||
                addressDto.getCountry().isEmpty() ||
                addressDto.getCity().isEmpty() ||
                addressDto.getStreet().isEmpty() ||
                addressDto.getHouseNumber().isEmpty()) {
            throw new IncompleteAddressException("All mandatory fields (name, surname, country, city, street, houseNumber) must be provided");
        }
    }
}