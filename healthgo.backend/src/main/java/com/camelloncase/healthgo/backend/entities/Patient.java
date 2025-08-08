package com.camelloncase.healthgo.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "patients")
public class Patient {

//    'code: 'PAC001', name: 'João Silva', age: 65, alert: false, series: mkInitialSeries()'
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16, unique = true)
    private String code; // PAC001 (identificador técnico)

    @Column(nullable = false, length = 120)
    private String fullName; // PII → não expor cru em VISITOR

    private Integer age;
    private Integer hr;
    private Integer spo2;
    private Integer sys;
    private Integer dia;
    private Double temperature;
    private Instant lastUpdate;

}
