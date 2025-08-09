package com.camelloncase.healthgo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VitalIngestRequest {

    @NotBlank
    private String patientId;      // PAC001

    @NotBlank
    private String timestamp;      // ISO-8601 OU "HH:mm:ss.SS" (será normalizado)

    @NotNull
    private Integer heartRate;      // hr

    @NotNull
    private Double spo2;

    @NotNull
    private Integer systolicPressure;

    @NotNull
    private Integer diastolicPressure;

    @NotNull
    private Double temperature;

    @NotNull
    private Double respiratoryRate;

    @NotBlank
    private String status;         // NORMAL | ALERT

}
