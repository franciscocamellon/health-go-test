package com.camelloncase.healthgo.backend.service;

import com.camelloncase.healthgo.backend.entities.PasswordResetToken;
import com.camelloncase.healthgo.backend.repository.PasswordTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class JwtTokenService {

    private final PasswordTokenRepository passwordTokenRepository;

    public JwtTokenService(PasswordTokenRepository passwordTokenRepository) {
        System.out.println(passwordTokenRepository);
        this.passwordTokenRepository = passwordTokenRepository;
    }

    public String validatePasswordResetToken(String token) {

        final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : "valid";
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }

    public void deleteToken(String token) {
        PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
        passwordTokenRepository.delete(passToken);
    }
}
