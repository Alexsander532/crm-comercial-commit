package com.commit.crm.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashGenerator {

    @Test
    void generateAdminHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("admin123");
        System.out.println("========================================");
        System.out.println("BCrypt hash for 'admin123':");
        System.out.println(hash);
        System.out.println("========================================");
        assertTrue(encoder.matches("admin123", hash));
    }
}
