package com.commit.crm.service;

import com.commit.crm.dto.request.LoginRequest;
import com.commit.crm.dto.request.RegisterRequest;
import com.commit.crm.dto.response.LoginResponse;
import com.commit.crm.dto.response.UserResponse;
import com.commit.crm.model.User;
import com.commit.crm.repository.UserRepository;
import com.commit.crm.config.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .isActive(true)
                .build();

        User saved = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getRole());

        return LoginResponse.builder()
                .token(token)
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .user(toUserResponse(saved))
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
