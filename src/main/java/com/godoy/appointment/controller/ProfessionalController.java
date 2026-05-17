package com.godoy.appointment.controller;

import com.godoy.appointment.dto.request.ProfessionalRequest;
import com.godoy.appointment.dto.response.ProfessionalResponse;
import com.godoy.appointment.service.ProfessionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professionals")
@RequiredArgsConstructor
public class ProfessionalController {

    private final ProfessionalService professionalService;

    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(@RequestBody @Valid ProfessionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professionalService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> findAll() {
        return ResponseEntity.ok(professionalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(professionalService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> update(@PathVariable UUID id,
                                                       @RequestBody @Valid ProfessionalRequest request) {
        return ResponseEntity.ok(professionalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        professionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
