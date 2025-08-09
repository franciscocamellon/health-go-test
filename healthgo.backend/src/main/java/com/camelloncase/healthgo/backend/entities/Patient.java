package com.camelloncase.healthgo.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiers
    @Column(nullable = false)
    private String patientId; // e.g., PAC001

    @Column(name = "full_name_enc", nullable = false, columnDefinition = "text")
    private String fullNameEnc;

    @Column(name = "cpf_enc", nullable = false, columnDefinition = "text", unique = true)
    private String cpfEnc;

    // Vital signs
    @Column(nullable = false)
    private Integer heartRate; // bpm

    @Column(nullable = false)
    private Double spo2; // O₂ saturation (%)

    @Column(nullable = false)
    private Integer systolicPressure; // mmHg

    @Column(nullable = false)
    private Integer diastolicPressure; // mmHg

    @Column(nullable = false)
    private Double temperature; // °C

    @Column(nullable = false)
    private Double respiratoryRate; // rpm

    @Column(nullable = false)
    private String status; // NORMAL or ALERT

    @Column(nullable = false)
    private LocalDateTime timestamp; // data collection time

    @Transient
    public String initialsFromDecrypted(String fullName) {
        if (fullName == null || fullName.isBlank()) return "—";
        String[] parts = fullName.trim().split("\\s+");
        String f = parts[0].substring(0,1).toUpperCase();
        String l = parts.length > 1 ? parts[parts.length-1].substring(0,1).toUpperCase() : "";
        return l.isBlank() ? (f + ".") : (f + ". " + l + ".");
    }
}