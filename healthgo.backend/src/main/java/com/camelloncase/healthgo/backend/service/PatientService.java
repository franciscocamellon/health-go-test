package com.camelloncase.healthgo.backend.service;

import com.camelloncase.healthgo.backend.dto.PatientDTO;
import com.camelloncase.healthgo.backend.entities.Patient;
import com.camelloncase.healthgo.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repo;

    public List<PatientDTO> listForRole(Authentication auth) {
        boolean doctor = hasRole(auth, "DOCTOR");
        return repo.findAll().stream()
                .map(p -> PatientDTO.from(p, doctor))
                .toList();
    }

    public PatientDTO getOneForRole(Long id, Authentication auth) {
        boolean doctor = hasRole(auth, "DOCTOR");
        Patient p = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return PatientDTO.from(p, doctor);
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}

