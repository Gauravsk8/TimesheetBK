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
        client.isActive();
        Clients savedClient = clientsRepository.save(client);
        return String.format(MessageConstants.CLIENT_CREATED, savedClient.getId());
    }

    @Override
    public List<ClientResponseDto> getAllClients() {
        List<Clients> activeClients = clientsRepository.findByIsActiveTrue();

        if (activeClients.isEmpty()) {
            throw new TimeSheetException(
                    errorCode.NOT_FOUND_ERROR,
                    errorMessage.NO_ACTIVE_CLIENTS_FOUND
            );
        }

        return activeClients.stream()
                .map(client -> new ClientResponseDto(
                        client.getId(),
                        client.getName(),
                        client.getContactPerson(),
                        client.getContactEmail(),
                        client.getAddress(),
                        client.isActive()
                ))
                .toList();
    }


    @Override
    public Optional<ClientResponseDto> getClientById(Long id) {
        Clients client = clientsRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.CLIENT_NOT_FOUND, id)));

        ClientResponseDto dto = new ClientResponseDto(
                client.getId(),
                client.getName(),
                client.getContactPerson(),
                client.getContactEmail(),
                client.getAddress(),
                client.isActive()
        );

        return Optional.of(dto);
    }


    @Override
    public String updateClient(Long id, ClientDto dto) throws TimeSheetException {
        Clients client = clientsRepository.findByIdAndIsActiveTrue(id)
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
    public String updateClientStatus(Long id, boolean active) throws TimeSheetException {
        Clients client = clientsRepository.findById(id)
                .orElseThrow(() -> new TimeSheetException(
                        errorCode.NOT_FOUND_ERROR,
                        String.format(errorMessage.CLIENT_NOT_FOUND, id)));


        client.setActive(active);
        Clients savedClient = clientsRepository.save(client);
        return String.format(MessageConstants.CLIENT_STATUS_UPDATED, savedClient.getName());
    }
}
