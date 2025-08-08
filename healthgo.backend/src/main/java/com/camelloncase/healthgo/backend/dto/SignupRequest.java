package com.camelloncase.healthgo.backend.dto;

import com.camelloncase.healthgo.backend.utils.UserRoles;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    private String username;
    private String password;
    private UserRoles role;
}
