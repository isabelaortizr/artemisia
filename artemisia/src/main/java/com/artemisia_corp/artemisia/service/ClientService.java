package com.artemisia_corp.artemisia.service;

import com.artemisia_corp.artemisia.entity.Client;
import com.artemisia_corp.artemisia.entity.dto.client.ClientDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.client.ClientRequestDto;
import com.artemisia_corp.artemisia.entity.dto.client.ClientUpdateDto;

import java.util.List;

public interface ClientService {
    List<Client> listAll();
    void save(ClientRequestDto client);
    void delete(ClientDeleteDto client);
    void update(ClientUpdateDto client);
}
