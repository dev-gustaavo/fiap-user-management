package br.com.user_management.application.usecases;

import br.com.user_management.adapters.inbound.rest.dto.response.LoginResponse;
import br.com.user_management.application.ports.outbound.AuthOutboundPort;
import br.com.user_management.application.usecase.AuthUseCase;
import br.com.user_management.core.domain.Login;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private AuthOutboundPort authOutboundPort;

    @InjectMocks
    private AuthUseCase authUseCase;

    private Login login;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        login = new Login("user@test.com", "password123");
        loginResponse = new LoginResponse("access_token", "refresh_token", "3600", 10L);
    }

    @Test
    void shouldAuthenticateSuccessfully() {
        // Given
        when(authOutboundPort.authenticateWithKeycloak(any(Login.class)))
                .thenReturn(loginResponse);

        // When
        LoginResponse result = authUseCase.authenticate(login);

        // Then
        assertNotNull(result);
        assertEquals(loginResponse.getAccessToken(), result.getAccessToken());
        assertEquals(loginResponse.getRefreshToken(), result.getRefreshToken());
        assertEquals(loginResponse.getExpiresIn(), result.getExpiresIn());

        verify(authOutboundPort, times(1)).authenticateWithKeycloak(login);
    }

    @Test
    void shouldPassLoginObjectToOutboundPort() {
        // Given
        when(authOutboundPort.authenticateWithKeycloak(login))
                .thenReturn(loginResponse);

        // When
        authUseCase.authenticate(login);

        // Then
        verify(authOutboundPort).authenticateWithKeycloak(login);
    }

    @Test
    void shouldReturnNullWhenOutboundPortReturnsNull() {
        // Given
        when(authOutboundPort.authenticateWithKeycloak(any(Login.class)))
                .thenReturn(null);

        // When
        LoginResponse result = authUseCase.authenticate(login);

        // Then
        assertNull(result);
        verify(authOutboundPort, times(1)).authenticateWithKeycloak(login);
    }

    @Test
    void shouldPropagateExceptionFromOutboundPort() {
        // Given
        RuntimeException exception = new RuntimeException("Authentication failed");
        when(authOutboundPort.authenticateWithKeycloak(any(Login.class)))
                .thenThrow(exception);

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> authUseCase.authenticate(login));

        assertEquals("Authentication failed", thrown.getMessage());
        verify(authOutboundPort, times(1)).authenticateWithKeycloak(login);
    }
}