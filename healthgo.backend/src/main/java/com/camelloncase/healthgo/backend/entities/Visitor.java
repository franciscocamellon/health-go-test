package com.camelloncase.healthgo.backend.entities;

import com.camelloncase.healthgo.backend.utils.UserRoles;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("VISITOR")
@Getter
@Setter
@NoArgsConstructor
public class Visitor extends HospitalUser {

    public Visitor(String username, String password) {
        super(username, password, UserRoles.VISITOR);
    }
}
