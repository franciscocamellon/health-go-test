package com.camelloncase.healthgo.backend.service;

import com.camelloncase.healthgo.backend.entities.HospitalUser;
import com.camelloncase.healthgo.backend.repository.UserRepository;
import com.camelloncase.healthgo.backend.utils.UserRoles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HospitalUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with this email: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + getUserRole(user)));

        return new User(user.getUsername(), user.getPassword(), authorities);
    }

    private UserRoles getUserRole(HospitalUser user) {
        return switch (String.valueOf(user.getRole())) {
            case "DOCTOR" -> UserRoles.DOCTOR;
            case "VISITOR" -> UserRoles.VISITOR;
            default -> null;
        };
    }
}
