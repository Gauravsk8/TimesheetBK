package com.example.timesheet.controller;

import com.example.common.annotations.RequiresKeycloakAuthorization;
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

    @PostMapping("/client")
    public ResponseEntity<String> createClient(@RequestBody ClientDto clientDto) {
        String response = clientService.createClient(clientDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponseDto>> getAllClients() {
        List<ClientResponseDto> response = clientService.getAllClients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<ClientResponseDto> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<String> updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        try {
            String updatedClient = clientService.updateClient(id, clientDto);
            return ResponseEntity.ok(updatedClient);
        } catch (TimeSheetException e) {
            throw new TimeSheetException(e.getErrorCode(), e.getMessage());
        }
    }
    @PutMapping("/client/{id}/status")
    public ResponseEntity<String> updateClientStatus(
            @PathVariable Long id,
            @RequestParam String newStatus) {

        String response = clientService.updateClientStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }
}

