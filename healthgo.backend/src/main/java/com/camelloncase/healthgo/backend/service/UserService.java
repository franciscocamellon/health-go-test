package com.camelloncase.healthgo.backend.service;


import com.camelloncase.healthgo.backend.dto.SignupRequest;
import com.camelloncase.healthgo.backend.entities.Doctor;
import com.camelloncase.healthgo.backend.entities.HospitalUser;
import com.camelloncase.healthgo.backend.entities.PasswordResetToken;
import com.camelloncase.healthgo.backend.entities.Visitor;
import com.camelloncase.healthgo.backend.repository.PasswordTokenRepository;
import com.camelloncase.healthgo.backend.repository.UserRepository;
import com.camelloncase.healthgo.backend.utils.UserRoles;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Collection<HospitalUser> getUsers() {
        return userRepository.findAll();
    }

    public Optional<HospitalUser> getUserById(Integer id) {
        return userRepository.findById(id);
    }


    public HospitalUser createUser(SignupRequest signupRequest) {

        if(userRepository.existsByUsername(signupRequest.getUsername())) {
            return null;
        }

        HospitalUser newHospitalUser = instantiateUserByRole(signupRequest.getRole());
        BeanUtils.copyProperties(signupRequest, newHospitalUser);

        String encodedPassword = passwordEncoder.encode(newHospitalUser.getPassword());
        newHospitalUser.setPassword(encodedPassword);

        return userRepository.save(newHospitalUser);
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found!");
        }
        userRepository.deleteById(id);
    }

    private HospitalUser instantiateUserByRole(UserRoles role) {
        return switch (role) {
            case UserRoles.DOCTOR -> new Doctor();
            case UserRoles.VISITOR -> new Visitor();
        };
    }

}
