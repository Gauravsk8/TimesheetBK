package com.example.timesheet.service.Serviceimpl;


import com.example.common.constants.MessageConstants;
import com.example.common.constants.errorCode;
import com.example.common.constants.errorMessage;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.Repository.ClientsRepository;
import com.example.timesheet.dto.request.ClientDto;
import com.example.timesheet.dto.response.ClientResponseDto;
import com.example.timesheet.enums.Status;
import com.example.timesheet.models.Clients;
import com.example.timesheet.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientsRepository clientsRepository;

    @Override
    public String createClient(ClientDto dto) {
        Clients client = new Clients();
        client.setName(dto.getName());
        client.setContactPerson(dto.getContactPerson());
        client.setContactEmail(dto.getContactEmail());
        client.setAddress(dto.getAddress());
        client.setStatus(Status.ACTIVATE);
        Clients savedClient = clientsRepository.save(client);
        return String.format(MessageConstants.CLIENT_CREATED, savedClient.getId());
    }

    @Override
    public List<ClientResponseDto> getAllClients() {
        return clientsRepository.findAll()
                .stream()
                .map(client -> new ClientResponseDto(
                        client.getId(),
                        client.getName(),
                        client.getContactPerson(),
                        client.getContactEmail(),
                        client.getAddress(),
                        client.getStatus()
                ))
                .toList();
    }

    @Override
    public Optional<ClientResponseDto> getClientById(Long id) {
        return clientsRepository.findById(id)
                .map(client -> new ClientResponseDto(
                        client.getId(),
                        client.getName(),
                        client.getContactPerson(),
                        client.getContactEmail(),
                        client.getAddress(),
                        client.getStatus()
                ));
    }

    @Override
    public String updateClient(Long id, ClientDto dto) throws TimeSheetException {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.CLIENT_NOT_FOUND, id)));

        client.setName(dto.getName());
        client.setContactPerson(dto.getContactPerson());
        client.setContactEmail(dto.getContactEmail());
        client.setAddress(dto.getAddress());
        Clients savedClient = clientsRepository.save(client);
        return String.format(MessageConstants.CLIENT_UPDATED, savedClient.getName());
    }

    @Override
    public String updateClientStatus(Long id, String newStatus) throws TimeSheetException {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.CLIENT_NOT_FOUND, id)));

        Status statusEnum;
        try {
            statusEnum = Status.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TimeSheetException(
                    errorCode.NOT_FOUND_ERROR,
                    errorMessage.STATUS_NOT_FOUND + newStatus);
        }

        client.setStatus(statusEnum);
        Clients savedClient = clientsRepository.save(client);
        return String.format(MessageConstants.CLIENT_STATUS_UPDATED, savedClient.getName());
    }
}
