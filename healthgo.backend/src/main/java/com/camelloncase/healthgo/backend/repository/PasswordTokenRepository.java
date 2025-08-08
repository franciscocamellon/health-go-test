package com.camelloncase.healthgo.backend.repository;

import com.camelloncase.healthgo.backend.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordTokenRepository extends CrudRepository<PasswordResetToken, Integer> {

    @Query("SELECT p FROM PasswordResetToken p WHERE p.blogUser.id = :userId")
    PasswordResetToken findTokenByHospitalUser(Integer userId);

    PasswordResetToken findByToken(String token);

}
