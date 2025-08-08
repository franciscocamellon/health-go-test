package com.camelloncase.healthgo.backend.repository;

import com.camelloncase.healthgo.backend.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByCode(String code);

    boolean existsByCode(String code);
}