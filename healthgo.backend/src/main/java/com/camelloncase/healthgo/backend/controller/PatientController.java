package com.camelloncase.healthgo.backend.controller;

import com.camelloncase.healthgo.backend.dto.PatientRequest;
import com.camelloncase.healthgo.backend.dto.PatientResponse;
import com.camelloncase.healthgo.backend.dto.VitalIngestRequest;
import com.camelloncase.healthgo.backend.entities.Patient;
import com.camelloncase.healthgo.backend.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ---- Médicos criam pacientes (com PII)
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@RequestBody @Valid PatientRequest req) {
        return patientService.create(req);
    }

    // ---- Ingest de vitais (sem PII). Por padrão deixei DOCTOR; ajuste se for o simulador técnico.
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.OK)
    public void ingest(@RequestBody @Valid VitalIngestRequest req) {
        patientService.ingest(req);
    }

    // ---- Lista para dashboard (LGPD aplicado no service)
    @GetMapping
    public List<PatientResponse> list(Authentication auth) {
        return patientService.list(auth);
    }

    // ---- Detalhe (LGPD aplicado no service)
    @GetMapping("/{id}")
    public PatientResponse getOne(@PathVariable Long id, Authentication auth) {
        return patientService.getOne(id, auth);
    }

    // Stream SSE (médico e visitante podem ouvir)
    @GetMapping(value="/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR','VISITOR')")
    public SseEmitter stream() {
        return patientService.subscribe();
    }
}
