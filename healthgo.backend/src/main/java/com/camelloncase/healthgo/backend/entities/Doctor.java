package com.camelloncase.healthgo.backend.entities;

import com.camelloncase.healthgo.backend.utils.UserRoles;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@DiscriminatorValue("DOCTOR")
@Getter @Setter
@NoArgsConstructor
public class Doctor extends HospitalUser {

    public Doctor(String username, String password) {
        super(username, password, UserRoles.DOCTOR);
    }
}