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

    @Column(nullable = false)
    private String fullName; // e.g., João Silva

    @Column(nullable = false, unique = true)
    private String cpf; // Brazilian document, will be encrypted

    // Vital signs
    @Column(nullable = false)
    private Integer heartRate; // bpm

    @Column(nullable = false)
    private Integer spo2; // O₂ saturation (%)

    @Column(nullable = false)
    private Integer systolicPressure; // mmHg

    @Column(nullable = false)
    private Integer diastolicPressure; // mmHg

    @Column(nullable = false)
    private Double temperature; // °C

    @Column(nullable = false)
    private Integer respiratoryRate; // rpm

    @Column(nullable = false)
    private String status; // NORMAL or ALERT

    @Column(nullable = false)
    private LocalDateTime timestamp; // data collection time

    // Utility for VISITOR role to show only initials
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            initials.append(part.charAt(0));
        }
        return initials.toString().toUpperCase();
    }
}