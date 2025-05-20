package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Client;
import com.artemisia_corp.artemisia.entity.Company;
import com.artemisia_corp.artemisia.entity.dto.client.ClientDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.client.ClientRequestDto;
import com.artemisia_corp.artemisia.entity.dto.client.ClientUpdateDto;
import com.artemisia_corp.artemisia.repository.ClientRepository;
import com.artemisia_corp.artemisia.service.ClientService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {
    private ClientRepository clientRepository;

    @Override
    public List<Client> listAll() {
        return this.clientRepository.findAll();
    }

    @Override
    public void save(ClientRequestDto client) {
        this.clientRepository.save(Client.builder()
                .complete_name(client.getName())
                .nit(client.getNit())
                .state(client.getState())
                .build()
        );
    }

    @Override
    public void delete(ClientDeleteDto client) {
        this.clientRepository.deleteById(client.getId());
    }

    @Override
    public void update(ClientUpdateDto client) {
        Client currentClient = this.clientRepository.findById(client.getId())
                .orElseThrow(() -> new RuntimeException("Company with ID " + client.getId() + " not found"));

        currentClient.setComplete_name(client.getName());
        currentClient.setNit(client.getNit());
        currentClient.setState(client.getState());
        this.clientRepository.save(currentClient);

    }
}
