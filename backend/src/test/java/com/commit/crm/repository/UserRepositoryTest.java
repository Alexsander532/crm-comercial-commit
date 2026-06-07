package com.commit.crm.repository;

import com.commit.crm.model.User;
import com.commit.crm.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldFindByEmail() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Teste")
                .email("teste@commit.com")
                .passwordHash("hash")
                .role(UserRole.AQUISICAO)
                .build();

        when(userRepository.findByEmail("teste@commit.com")).thenReturn(Optional.of(user));

        Optional<User> found = userRepository.findByEmail("teste@commit.com");

        assertTrue(found.isPresent());
        assertEquals("Teste", found.get().getName());
        verify(userRepository).findByEmail("teste@commit.com");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        when(userRepository.findByEmail("naoexiste@commit.com")).thenReturn(Optional.empty());

        Optional<User> found = userRepository.findByEmail("naoexiste@commit.com");

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldCheckIfEmailExists() {
        when(userRepository.existsByEmail("teste@commit.com")).thenReturn(true);
        when(userRepository.existsByEmail("outro@commit.com")).thenReturn(false);

        assertTrue(userRepository.existsByEmail("teste@commit.com"));
        assertFalse(userRepository.existsByEmail("outro@commit.com"));
    }
}
