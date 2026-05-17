package com.godoy.appointment.controller;

import com.godoy.appointment.dto.request.AppointmentRequest;
import com.godoy.appointment.dto.response.AppointmentResponse;
import com.godoy.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@RequestBody @Valid AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> findAll() {
        return ResponseEntity.ok(appointmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AppointmentResponse>> findAllByClientId(@PathVariable UUID clientId) {
        return ResponseEntity.ok(appointmentService.findAllByClientId(clientId));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<AppointmentResponse>> findAllByProfessionalId(@PathVariable UUID professionalId) {
        return ResponseEntity.ok(appointmentService.findAllByClientId(professionalId));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.confirm(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.complete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
