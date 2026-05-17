package com.godoy.appointment.controller;

import com.godoy.appointment.dto.request.ServiceOfferedRequest;
import com.godoy.appointment.dto.response.ServiceOfferedResponse;
import com.godoy.appointment.service.ServiceOfferedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceOfferedController {

    private final ServiceOfferedService serviceOfferedService;

    @PostMapping
    public ResponseEntity<ServiceOfferedResponse> create(@RequestBody @Valid ServiceOfferedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOfferedService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ServiceOfferedResponse>> findAll() {
        return ResponseEntity.ok(serviceOfferedService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOfferedResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceOfferedService.findById(id));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<ServiceOfferedResponse>> findAllByProfessionalId(@PathVariable UUID professionalId) {
        return ResponseEntity.ok(serviceOfferedService.findAllByProfessionalId(professionalId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOfferedResponse> update(@PathVariable UUID id,
                                                         @RequestBody @Valid ServiceOfferedRequest request) {
        return ResponseEntity.ok(serviceOfferedService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceOfferedService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
