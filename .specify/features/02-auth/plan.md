# Plano: 02 — Autenticação e Autorização

**Branch**: `feature/02-auth` | **Data**: 2026-06-06
**Depende de**: 01-setup-projeto
**Bloqueia**: 03-leads-crud, 04-pipeline-kanban, 05-contatos, 06-tarefas, 07-timeline, 08-dashboard
**Worktree**: `.worktrees/feature/02-auth`

---

## Objetivo

Implementar autenticação JWT com Spring Security e autorização hierárquica
(DIRETOR > GERENTE > FUNCIONÁRIO). Criar endpoints de login, registro de
usuários, e proteção de rotas com roles.

---

## Contexto

Toda a aplicação depende de auth. Sem isso, não temos como:
- Identificar quem criou o lead
- Restringir acesso por papel
- Rastrear quem fez cada ação (timeline)

---

## Tasks

### Task 1: Criar User entity e repository

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/model/User.java`
- Criar: `backend/src/main/java/com/commit/crm/model/UserRole.java`
- Criar: `backend/src/main/java/com/commit/crm/repository/UserRepository.java`

**Passo 1: Criar User.java**

```java
package com.commit.crm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

**Passo 2: Criar UserRole.java**

```java
package com.commit.crm.model;

public enum UserRole {
    DIRETOR,
    GERENTE_AQUISICAO,
    GERENTE_PROSPECCAO,
    AQUISICAO,
    PROSPECCAO
}
```

**Passo 3: Criar UserRepository.java**

```java
package com.commit.crm.repository;

import com.commit.crm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

**Passo 4: Testar**

```bash
cd backend
mvn test -Dtest=UserRepositoryTest
```

**Passo 5: Commit**

```bash
git add backend/src/main/java/com/commit/crm/model/
git add backend/src/main/java/com/commit/crm/repository/
git commit -m "feat(auth): add User entity, UserRole enum, and UserRepository"
```

---

### Task 2: Criar DTOs de Auth

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/dto/request/LoginRequest.java`
- Criar: `backend/src/main/java/com/commit/crm/dto/request/RegisterRequest.java`
- Criar: `backend/src/main/java/com/commit/crm/dto/response/LoginResponse.java`
- Criar: `backend/src/main/java/com/commit/crm/dto/response/ApiResponse.java`

**Passo 1: LoginRequest.java**

```java
package com.commit.crm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
}
```

**Passo 2: RegisterRequest.java**

```java
package com.commit.crm.dto.request;

import com.commit.crm.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
    @NotNull
    private UserRole role;
}
```

**Passo 3: LoginResponse.java**

```java
package com.commit.crm.dto.response;

import com.commit.crm.model.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LoginResponse {
    private String token;
    private Long expiresIn;
    private UserData user;

    @Data
    @Builder
    public static class UserData {
        private UUID id;
        private String name;
        private String email;
        private UserRole role;
    }
}
```

**Passo 4: ApiResponse.java**

```java
package com.commit.crm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> {
    private T data;
    private String message;
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .data(data)
            .message(message)
            .timestamp(Instant.now())
            .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Sucesso");
    }
}
```

**Passo 5: Commit**

```bash
git add backend/src/main/java/com/commit/crm/dto/
git commit -m "feat(auth): add auth DTOs (LoginRequest, RegisterRequest, LoginResponse, ApiResponse)"
```

---

### Task 3: Implementar JWT

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/config/security/JwtTokenProvider.java`
- Criar: `backend/src/main/java/com/commit/crm/config/security/JwtAuthenticationFilter.java`
- Criar: `backend/src/main/java/com/commit/crm/config/security/CustomUserDetailsService.java`

**Passo 1: JwtTokenProvider.java**

```java
package com.commit.crm.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long expiration;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(UUID userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.get("role", String.class);
    }
}
```

**Passo 2: JwtAuthenticationFilter.java**

```java
package com.commit.crm.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (token != null && tokenProvider.validateToken(token)) {
            String userId = tokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
            auth.setDetails(request);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

**Passo 3: CustomUserDetailsService.java**

```java
package com.commit.crm.config.security;

import com.commit.crm.model.User;
import com.commit.crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
            user.getId().toString(),
            user.getPasswordHash(),
            user.getIsActive(),
            true, true, true,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
```

**Passo 4: Testar**

```bash
mvn test -Dtest=JwtTokenProviderTest
```

**Passo 5: Commit**

```bash
git add backend/src/main/java/com/commit/crm/config/security/
git commit -m "feat(auth): implement JWT token provider, filter, and user details service"
```

---

### Task 4: Configurar Spring Security

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/config/security/SecurityConfig.java`

**Passo 1: SecurityConfig.java**

```java
package com.commit.crm.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints (public)
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").hasRole("DIRETOR")
                .requestMatchers("/api/auth/**").authenticated()

                // Users (DIRETOR only)
                .requestMatchers("/api/users/**").hasRole("DIRETOR")

                // Leads
                .requestMatchers(HttpMethod.POST, "/api/leads").hasAnyRole("AQUISICAO", "DIRETOR", "GERENTE_AQUISICAO")
                .requestMatchers(HttpMethod.PUT, "/api/leads/**").hasAnyRole("AQUISICAO", "DIRETOR", "GERENTE_AQUISICAO")
                .requestMatchers(HttpMethod.PATCH, "/api/leads/**/status").hasAnyRole("PROSPECCAO", "DIRETOR", "GERENTE_PROSPECCAO")
                .requestMatchers("/api/leads/**").authenticated()

                // Others
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/main/java/com/commit/crm/config/security/SecurityConfig.java
git commit -m "feat(auth): configure Spring Security with JWT, CORS, and role-based access"
```

---

### Task 5: Criar AuthController e AuthService

**Arquivos:**
- Criar: `backend/src/main/java/com/commit/crm/controller/AuthController.java`
- Criar: `backend/src/main/java/com/commit/crm/service/AuthService.java`

**Passo 1: AuthService.java**

```java
package com.commit.crm.service;

import com.commit.crm.config.security.JwtTokenProvider;
import com.commit.crm.dto.request.LoginRequest;
import com.commit.crm.dto.request.RegisterRequest;
import com.commit.crm.dto.response.LoginResponse;
import com.commit.crm.model.User;
import com.commit.crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authManager;

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow();

        String token = tokenProvider.generateToken(
            user.getId(), user.getEmail(), user.getRole().name()
        );

        return LoginResponse.builder()
            .token(token)
            .expiresIn(86400L)
            .user(LoginResponse.UserData.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build())
            .build();
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .isActive(true)
            .build();

        return userRepository.save(user);
    }
}
```

**Passo 2: AuthController.java**

```java
package com.commit.crm.controller;

import com.commit.crm.dto.request.LoginRequest;
import com.commit.crm.dto.request.RegisterRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.LoginResponse;
import com.commit.crm.model.User;
import com.commit.crm.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login realizado com sucesso"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(user, "Usuário criado com sucesso"));
    }
}
```

**Passo 3: Commit**

```bash
git add backend/src/main/java/com/commit/crm/controller/AuthController.java
git add backend/src/main/java/com/commit/crm/service/AuthService.java
git commit -m "feat(auth): add AuthController and AuthService with login and register"
```

---

### Task 6: Testes de Auth

**Arquivos:**
- Criar: `backend/src/test/java/com/commit/crm/service/AuthServiceTest.java`
- Criar: `backend/src/test/java/com/commit/crm/controller/AuthControllerTest.java`

**Passo 1: AuthServiceTest.java**

```java
package com.commit.crm.service;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authManager;
    @InjectMocks AuthService authService;

    @Test
    void register_shouldCreateUser() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Teste");
        request.setEmail("teste@test.com");
        request.setPassword("123456");
        request.setRole(UserRole.AQUISICAO);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = authService.register(request);

        assertNotNull(result);
        assertEquals("Teste", result.getName());
    }
}
```

**Passo 2: Commit**

```bash
git add backend/src/test/java/com/commit/crm/service/AuthServiceTest.java
git commit -m "test(auth): add AuthService unit tests"
```

---

## Resumo

| Task | O que entrega |
|------|---------------|
| 1 | User entity, UserRole enum, UserRepository |
| 2 | DTOs de auth (LoginRequest, RegisterRequest, LoginResponse, ApiResponse) |
| 3 | JWT (TokenProvider, Filter, UserDetailsService) |
| 4 | Spring Security config (CORS, roles, stateless) |
| 5 | AuthController e AuthService (login + register) |
| 6 | Testes unitários |

---

## Próxima Feature

→ [03-leads-crud/plan.md](03-leads-crud/plan.md)
