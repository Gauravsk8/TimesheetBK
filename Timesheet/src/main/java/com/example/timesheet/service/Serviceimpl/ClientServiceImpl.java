package com.example.timesheet.service.Serviceimpl;


import com.example.common.constants.MessageConstants;
import com.example.common.constants.ErrorCode;
import com.example.common.constants.ErrorMessage;
import com.example.common.dto.PageRequestDto;
import com.example.common.dto.response.PagedResponse;
import com.example.common.exceptions.TimeSheetException;
import com.example.common.utils.FilterSpecificationBuilder;
import com.example.common.utils.SortUtil;
import com.example.timesheet.Repository.ClientsRepository;
import com.example.timesheet.dto.request.ClientDto;
import com.example.timesheet.dto.response.ClientResponseDto;
import com.example.timesheet.models.Clients;
import com.example.timesheet.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public PagedResponse<ClientResponseDto> getAllClients(PageRequestDto pageRequestDto) {
        Pageable pageable = PageRequest.of(
                pageRequestDto.getPage(),
                pageRequestDto.getSize(),
                SortUtil.getSort(pageRequestDto.getSort())
        );

        Specification<Clients> spec = new FilterSpecificationBuilder<Clients>()
                .build(pageRequestDto.getFilter());

        Specification<Clients> isActiveSpec = (root, query, cb) ->
                cb.isTrue(root.get("isActive"));

        Specification<Clients> finalSpec = Specification.where(isActiveSpec).and(spec);

        Page<Clients> clientPage = clientsRepository.findAll(spec, pageable);

        if (clientPage.isEmpty()) {
            throw new TimeSheetException(
                    ErrorCode.NOT_FOUND_ERROR,
                    ErrorMessage.NO_ACTIVE_CLIENTS_FOUND
            );
        }

        List<ClientResponseDto> content = clientPage.getContent().stream()
                .map(client -> new ClientResponseDto(
                        client.getId(),
                        client.getName(),
                        client.getContactPerson(),
                        client.getContactEmail(),
                        client.getAddress(),
                        client.isActive()
                )).toList();

        return new PagedResponse<>(
                content,
                clientPage.getNumber(),
                clientPage.getSize(),
                clientPage.getTotalElements()
        );
    }




    @Override
    public Optional<ClientResponseDto> getClientById(Long id) {
        Clients client = clientsRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new TimeSheetException(
                        ErrorCode.NOT_FOUND_ERROR,
                        String.format(ErrorMessage.CLIENT_NOT_FOUND, id)));

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
                        ErrorCode.NOT_FOUND_ERROR,
                        String.format(ErrorMessage.CLIENT_NOT_FOUND, id)));

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
                        ErrorCode.NOT_FOUND_ERROR,
                        String.format(ErrorMessage.CLIENT_NOT_FOUND, id)));


        client.setActive(active);
        Clients savedClient = clientsRepository.save(client);
        return String.format(MessageConstants.CLIENT_STATUS_UPDATED, savedClient.getName());
    }
}
