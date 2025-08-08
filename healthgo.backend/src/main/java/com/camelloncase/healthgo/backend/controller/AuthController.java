package com.camelloncase.healthgo.backend.controller;


import com.camelloncase.healthgo.backend.dto.LoginRequest;
import com.camelloncase.healthgo.backend.dto.LoginResponse;
import com.camelloncase.healthgo.backend.dto.SignupRequest;
import com.camelloncase.healthgo.backend.entities.HospitalUser;
import com.camelloncase.healthgo.backend.repository.UserRepository;
import com.camelloncase.healthgo.backend.security.jwt.JwtUtil;
import com.camelloncase.healthgo.backend.service.AuthUserDetailsService;
import com.camelloncase.healthgo.backend.service.JwtTokenService;
import com.camelloncase.healthgo.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AuthUserDetailsService authUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    public AuthController(UserService userService, AuthenticationManager authenticationManager, AuthUserDetailsService authUserDetailsService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.authUserDetailsService = authUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody @Valid SignupRequest signupRequest) {

        HospitalUser createdUser = userService.createUser(signupRequest);

        if (createdUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create user");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword()));

        } catch (BadCredentialsException badCredentialsException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");

        } catch (DisabledException disabledException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("HospitalUser not activated");
        }

        final UserDetails userDetails = authUserDetailsService.loadUserByUsername(loginRequest.getUsername());

        Optional<HospitalUser> user = userRepository.findByUsername(userDetails.getUsername());

        final String jwt = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(jwt));
    }

}
