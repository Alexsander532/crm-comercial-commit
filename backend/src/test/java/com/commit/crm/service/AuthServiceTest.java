package com.commit.crm.service;

import com.commit.crm.config.security.JwtTokenProvider;
import com.commit.crm.dto.request.LoginRequest;
import com.commit.crm.dto.request.RegisterRequest;
import com.commit.crm.dto.response.LoginResponse;
import com.commit.crm.model.User;
import com.commit.crm.model.UserRole;
import com.commit.crm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUser() {
        when(userRepository.existsByEmail("novo@commit.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded-hash");
        when(jwtTokenProvider.generateToken(any(UUID.class), any(UserRole.class)))
                .thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            return User.builder()
                    .id(UUID.randomUUID())
                    .name(u.getName())
                    .email(u.getEmail())
                    .passwordHash(u.getPasswordHash())
                    .role(u.getRole())
                    .isActive(true)
                    .build();
        });

        RegisterRequest request = new RegisterRequest(
                "Novo Usuário", "novo@commit.com", "senha123", UserRole.AQUISICAO);

        LoginResponse response = authService.register(request);

        assertNotNull(response.token());
        assertEquals("jwt-token", response.token());
        assertEquals("Novo Usuário", response.user().name());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("existente@commit.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest(
                "Teste", "existente@commit.com", "senha123", UserRole.AQUISICAO);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Teste")
                .email("teste@commit.com")
                .passwordHash("encoded")
                .role(UserRole.DIRETOR)
                .isActive(true)
                .build();

        when(userRepository.findByEmail("teste@commit.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(userId, UserRole.DIRETOR)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        LoginRequest request = new LoginRequest("teste@commit.com", "senha123");

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals("Teste", response.user().name());
        verify(authenticationManager).authenticate(any());
    }
}
