package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
import com.example.common.dto.PageRequestDto;
import com.example.common.dto.response.PagedResponse;
import com.example.common.exceptions.TimeSheetException;
import com.example.timesheet.dto.request.ClientDto;
import com.example.timesheet.dto.response.ClientResponseDto;
import com.example.timesheet.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/clients")
    @RequiresKeycloakAuthorization(resource = "tms:admin", scope = "tms:client:add")
    public ResponseEntity<String> createClient(@RequestBody ClientDto clientDto) {
        String response = clientService.createClient(clientDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clients/Page")
    @RequiresKeycloakAuthorization(resource = "tms:adminccmpm", scope = "tms:client:get")
    public ResponseEntity<PagedResponse<ClientResponseDto>> getAllClientsPaged(
            @RequestBody PageRequestDto pageRequestDto) {
        return ResponseEntity.ok(clientService.getAllClients(pageRequestDto));
    }

    @GetMapping("/clients/{id}")
    @RequiresKeycloakAuthorization(resource = "tms:admin", scope = "tms:client:get")
    public ResponseEntity<ClientResponseDto> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/clients/{id}")
    @RequiresKeycloakAuthorization(resource = "tms:admin", scope = "tms:client:update")
    public ResponseEntity<String> updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        try {
            String updatedClient = clientService.updateClient(id, clientDto);
            return ResponseEntity.ok(updatedClient);
        } catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }
    @PutMapping("/clients/{id}/status")
    @RequiresKeycloakAuthorization(resource = "tms:admin", scope = "tms:client:update")
    public ResponseEntity<String> updateClientStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        String response = clientService.updateClientStatus(id, active);
        return ResponseEntity.ok(response);
    }
}

