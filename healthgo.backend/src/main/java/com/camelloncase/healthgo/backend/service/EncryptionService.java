package com.camelloncase.healthgo.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de Criptografia para conformidade com LGPD
 * Implementa criptografia AES-256-GCM para dados sensíveis
 * Gera pseudônimos para identificação segura
 */
@Slf4j
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    // Cache de pseudônimos para evitar reprocessamento
    private final Map<String, String> pseudonymCache = new HashMap<>();

    public EncryptionService(@Value("${app.encryption.key:}") String encryptionKeyBase64) {
        this.secureRandom = new SecureRandom();

        if (encryptionKeyBase64.isEmpty()) {
            // Gera uma chave para ambiente de desenvolvimento
            log.warn("Nenhuma chave de criptografia fornecida. Gerando chave temporária para desenvolvimento.");
            this.secretKey = generateKey();
            log.info("Chave gerada (Base64): {}", Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        } else {
            // Usa a chave fornecida via configuração
            byte[] decodedKey = Base64.getDecoder().decode(encryptionKeyBase64);
            this.secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
        }
    }

    /**
     * Criptografa dados sensíveis usando AES-256-GCM
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return null;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combina IV + dados criptografados
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);

        } catch (Exception e) {
            log.error("Erro ao criptografar dados: {}", e.getMessage());
            throw new RuntimeException("Falha na criptografia", e);
        }
    }

    /**
     * Descriptografa dados sensíveis
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);

            // Extrai IV e dados criptografados
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[decodedData.length - GCM_IV_LENGTH];

            System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(decodedData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decryptedData = cipher.doFinal(encrypted);
            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Erro ao descriptografar dados: {}", e.getMessage());
            throw new RuntimeException("Falha na descriptografia", e);
        }
    }

    /**
     * Gera um pseudônimo seguro para um dado sensível
     * Utiliza hash SHA-256 para criar identificador não reversível
     */
    public String generatePseudonym(String sensitiveData, String prefix) {
        if (sensitiveData == null || sensitiveData.isEmpty()) {
            return null;
        }

        // Verifica cache primeiro
        String cacheKey = prefix + ":" + sensitiveData;
        if (pseudonymCache.containsKey(cacheKey)) {
            return pseudonymCache.get(cacheKey);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sensitiveData.getBytes(StandardCharsets.UTF_8));

            // Usa apenas os primeiros 8 bytes do hash para criar um identificador curto
            String hashString = Base64.getEncoder().encodeToString(hash).substring(0, 8);
            String pseudonym = prefix + "_" + hashString.replaceAll("[^A-Za-z0-9]", "");

            // Cache o resultado
            pseudonymCache.put(cacheKey, pseudonym);

            return pseudonym;

        } catch (Exception e) {
            log.error("Erro ao gerar pseudônimo: {}", e.getMessage());
            throw new RuntimeException("Falha na geração de pseudônimo", e);
        }
    }

    /**
     * Mascara CPF para exibição segura (XXX.XXX.XXX-XX)
     */
    public String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return "XXX.XXX.XXX-XX";
        }

        // Remove formatação existente
        String cleanCpf = cpf.replaceAll("[^0-9]", "");

        if (cleanCpf.length() != 11) {
            return "XXX.XXX.XXX-XX";
        }

        // Mostra apenas os 2 últimos dígitos
        return "XXX.XXX.XXX-" + cleanCpf.substring(9);
    }

    /**
     * Gera hash para verificação de integridade de dados
     */
    public String generateDataHash(Object data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String dataString = data.toString();
            byte[] hash = digest.digest(dataString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Erro ao gerar hash de integridade: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gera uma nova chave de criptografia
     */
    private SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            log.error("Erro ao gerar chave de criptografia: {}", e.getMessage());
            throw new RuntimeException("Falha na geração de chave", e);
        }
    }

    /**
     * Valida se os dados criptografados podem ser descriptografados
     */
    public boolean validateEncryptedData(String encryptedData) {
        try {
            decrypt(encryptedData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Limpa cache de pseudônimos (para fins de segurança)
     */
    public void clearPseudonymCache() {
        pseudonymCache.clear();
        log.info("Cache de pseudônimos limpo");
    }
}
