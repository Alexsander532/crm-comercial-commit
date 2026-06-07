package com.commit.crm.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithBuilder() {
        User user = User.builder()
                .name("Diretor")
                .email("diretor@commit.com")
                .passwordHash("hash")
                .role(UserRole.DIRETOR)
                .isActive(true)
                .build();

        assertNotNull(user);
        assertEquals("Diretor", user.getName());
        assertEquals("diretor@commit.com", user.getEmail());
        assertEquals(UserRole.DIRETOR, user.getRole());
        assertTrue(user.getIsActive());
    }

    @Test
    void shouldDefaultIsActiveToTrue() {
        User user = User.builder()
                .name("Teste")
                .email("teste@commit.com")
                .passwordHash("hash")
                .role(UserRole.AQUISICAO)
                .build();

        assertTrue(user.getIsActive());
    }

    @Test
    void shouldAcceptAllRoles() {
        assertDoesNotThrow(() -> UserRole.valueOf("DIRETOR"));
        assertDoesNotThrow(() -> UserRole.valueOf("GERENTE_AQUISICAO"));
        assertDoesNotThrow(() -> UserRole.valueOf("GERENTE_PROSPECCAO"));
        assertDoesNotThrow(() -> UserRole.valueOf("AQUISICAO"));
        assertDoesNotThrow(() -> UserRole.valueOf("PROSPECCAO"));
    }
}
