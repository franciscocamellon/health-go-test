package com.camelloncase.healthgo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientRequest {
    @NotBlank
    private String patientId;  // PAC001

    @NotBlank
    private String fullName;   // João Silva

    @NotBlank
    private String cpf;        // 123.456.789-00 (vamos criptografar depois)

}
