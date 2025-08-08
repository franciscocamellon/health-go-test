package com.camelloncase.healthgo.backend.repository;

import com.camelloncase.healthgo.backend.entities.HospitalUser;
import com.camelloncase.healthgo.backend.entities.Doctor;
import com.camelloncase.healthgo.backend.entities.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<HospitalUser, Integer> {
    Optional<HospitalUser> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("select u from HospitalUser u where TYPE(u) = Doctor")
    List<Doctor> findAllDoctors();

    @Query("select u from HospitalUser u where TYPE(u) = Visitor")
    List<Visitor> findAllVisitors();
}

