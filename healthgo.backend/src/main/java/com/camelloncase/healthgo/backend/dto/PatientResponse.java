package com.camelloncase.healthgo.backend.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PatientResponse(
        Long id,
        String patientId,
        String displayName,           // fullName (doctor) OU iniciais (visitor)
        Integer heartRate,
        Integer spo2,
        Integer systolicPressure,
        Integer diastolicPressure,
        Double temperature,
        Integer respiratoryRate,
        String status,
        LocalDateTime timestamp
) {}
