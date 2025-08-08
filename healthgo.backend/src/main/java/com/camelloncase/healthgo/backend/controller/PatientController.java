package com.camelloncase.healthgo.backend.controller;

import com.camelloncase.healthgo.backend.dto.PatientDTO;
import com.camelloncase.healthgo.backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @GetMapping
    public List<PatientDTO> list(Authentication auth) {
        return service.listForRole(auth);
    }

    @GetMapping("/{id}")
    public PatientDTO getOne(@PathVariable Long id, Authentication auth) {
        return service.getOneForRole(id, auth);
    }

    // Exemplo de endpoint que SEMPRE exige acesso médico (PII explícita, export completo etc.)
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/{id}/export")
    public ResponseEntity<Resource> export(@PathVariable Long id) {
        // ...gera CSV/JSON com nome completo etc.
        return ResponseEntity.ok(/* resource */ null);
    }
}

