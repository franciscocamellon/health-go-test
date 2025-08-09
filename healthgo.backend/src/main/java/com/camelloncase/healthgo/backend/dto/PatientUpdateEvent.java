package com.camelloncase.healthgo.backend.dto;

import java.time.LocalDateTime;

public record PatientUpdateEvent(
        Long id, String patientId,
        Integer heartRate, Double spo2, Integer systolicPressure, Integer diastolicPressure,
        Double temperature, Double respiratoryRate, String status, LocalDateTime timestamp
) {}
