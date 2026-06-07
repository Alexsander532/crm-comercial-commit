package com.commit.crm.dto;

import com.commit.crm.dto.request.LoginRequest;
import com.commit.crm.dto.request.RegisterRequest;
import com.commit.crm.dto.response.ApiResponse;
import com.commit.crm.dto.response.LoginResponse;
import com.commit.crm.dto.response.UserResponse;
import com.commit.crm.model.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldValidateLoginRequest() {
        LoginRequest request = new LoginRequest("", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAcceptValidLoginRequest() {
        LoginRequest request = new LoginRequest("email@teste.com", "senha123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldAcceptValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest("Nome", "email@teste.com", "senha123", UserRole.AQUISICAO);
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldBuildApiResponseSuccess() {
        ApiResponse<String> response = ApiResponse.success("dados", "Sucesso");

        assertNotNull(response.timestamp());
        assertEquals("dados", response.data());
        assertEquals("Sucesso", response.message());
    }

    @Test
    void shouldBuildApiResponseError() {
        ApiResponse<String> response = ApiResponse.error("Erro");

        assertNotNull(response.timestamp());
        assertNull(response.data());
        assertEquals("Erro", response.message());
    }

    @Test
    void shouldBuildLoginResponse() {
        UserResponse user = UserResponse.builder()
                .id(UUID.randomUUID())
                .name("Teste")
                .email("teste@teste.com")
                .role(UserRole.DIRETOR)
                .build();

        LoginResponse response = LoginResponse.builder()
                .token("jwt-token")
                .expiresIn(86400000L)
                .user(user)
                .build();

        assertEquals("jwt-token", response.token());
        assertEquals(86400000L, response.expiresIn());
        assertEquals("Teste", response.user().name());
    }
}
