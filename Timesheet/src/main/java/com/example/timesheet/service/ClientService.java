package com.example.timesheet.service;

import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.dto.request.ClientDto;
import com.example.timesheet.dto.response.ClientResponseDto;

import java.util.List;
import java.util.Optional;

public interface ClientService {
    String createClient(ClientDto dto);
    List<ClientResponseDto> getAllClients();
    Optional<ClientResponseDto> getClientById(Long id);
    String updateClient(Long id, ClientDto dto) throws TimeSheetException;
    String updateClientStatus(Long id, boolean active) throws TimeSheetException;
}