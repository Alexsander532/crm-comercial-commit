package com.commit.crm.config.security;

import com.commit.crm.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "d2ViLXNlY3JldC1kby1jcm0tY29tZXJjaWFsLWNvbW1pdC1xdWUtZGV2ZS1zZXItdXNhZG8tZW0tcHJvZHVjYW8=",
                86400000L
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, UserRole.DIRETOR);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void shouldExtractUserIdFromToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, UserRole.AQUISICAO);

        UUID extractedId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(userId, extractedId);
    }

    @Test
    void shouldExtractRoleFromToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, UserRole.GERENTE_PROSPECCAO);

        String role = jwtTokenProvider.getRoleFromToken(token);
        assertEquals("GERENTE_PROSPECCAO", role);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "d2ViLXNlY3JldC1kby1jcm0tY29tZXJjaWFsLWNvbW1pdC1xdWUtZGV2ZS1zZXItdXNhZG8tZW0tcHJvZHVjYW8=",
                -1000L // já expirado
        );
        String token = shortLived.generateToken(UUID.randomUUID(), UserRole.DIRETOR);
        assertFalse(shortLived.validateToken(token));
    }
}
