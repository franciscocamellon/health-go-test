package com.camelloncase.healthgo.backend.dto;

import com.camelloncase.healthgo.backend.entities.Patient;

import java.time.Instant;

public record PatientDTO(
        Long id,
        String code,
        String displayName, // nome completo (médico) OU iniciais (visitor)
        Integer age,
        Integer hr,
        Integer spo2,
        Integer sys,
        Integer dia,
        Double temperature,
        Instant lastUpdate
) {
    public static PatientDTO from(Patient p, boolean canSeePii) {
        String name = canSeePii ? p.getFullName() : maskName(p.getFullName());
        return new PatientDTO(
                p.getId(), p.getCode(), name,
                p.getAge(), p.getHr(), p.getSpo2(), p.getSys(), p.getDia(), p.getTemperature(),
                p.getLastUpdate()
        );
    }

    private static String maskName(String full) {
        if (full == null || full.isBlank()) return "—";
        String[] parts = full.trim().split("\\s+");
        String first = parts[0].substring(0, 1).toUpperCase();
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1).toUpperCase() : "";
        return last.isBlank() ? (first + ".") : (first + ". " + last + ".");
    }
}

