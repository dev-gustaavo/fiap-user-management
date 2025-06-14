package br.com.user_management.application.usecases;

import br.com.user_management.adapters.inbound.rest.dto.response.TokenValidationResponse;
import br.com.user_management.application.ports.outbound.TokenOutboundPort;
import br.com.user_management.application.usecase.TokenValidationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenValidationUseCaseTest {

    @Mock
    private TokenOutboundPort tokenOutboundPort;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private TokenValidationUseCase tokenValidationUseCase;

    private String validToken;
    private String bearerToken;
    private Instant issuedAt;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        bearerToken = "Bearer " + validToken;
        issuedAt = Instant.now().minusSeconds(3600);
        expiresAt = Instant.now().plusSeconds(3600);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given
        when(tokenOutboundPort.validateJwtToken(validToken)).thenReturn(jwt);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Test User");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(
                Map.of("roles", List.of("USER", "ADMIN"))
        );
        when(jwt.getIssuedAt()).thenReturn(issuedAt);
        when(jwt.getExpiresAt()).thenReturn(expiresAt);

        // When
        TokenValidationResponse result = tokenValidationUseCase.validateToken(validToken);

        // Then
        assertTrue(result.isValid());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals(List.of("USER", "ADMIN"), result.getRoles());
        assertNotNull(result.getIssuedAt());
        assertNotNull(result.getExpiresAt());

        verify(tokenOutboundPort, times(1)).validateJwtToken(validToken);
    }

    @Test
    void shouldRemoveBearerPrefixFromToken() {
        // Given
        when(tokenOutboundPort.validateJwtToken(validToken)).thenReturn(jwt);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Test User");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(
                Map.of("roles", List.of("USER"))
        );
        when(jwt.getIssuedAt()).thenReturn(issuedAt);
        when(jwt.getExpiresAt()).thenReturn(expiresAt);

        // When
        TokenValidationResponse result = tokenValidationUseCase.validateToken(bearerToken);

        // Then
        assertTrue(result.isValid());
        verify(tokenOutboundPort, times(1)).validateJwtToken(validToken);
    }

    @Test
    void shouldReturnInvalidWhenJwtIsNull() {
        // Given
        when(tokenOutboundPort.validateJwtToken(anyString())).thenReturn(null);

        // When
        TokenValidationResponse result = tokenValidationUseCase.validateToken(validToken);

        // Then
        assertFalse(result.isValid());
        verify(tokenOutboundPort, times(1)).validateJwtToken(validToken);
    }

    @Test
    void shouldReturnInvalidWhenExceptionIsThrown() {
        // Given
        when(tokenOutboundPort.validateJwtToken(anyString()))
                .thenThrow(new RuntimeException("Token validation failed"));

        // When
        TokenValidationResponse result = tokenValidationUseCase.validateToken(validToken);

        // Then
        assertFalse(result.isValid());
        verify(tokenOutboundPort, times(1)).validateJwtToken(validToken);
    }

    @Test
    void shouldHandleEmptyRoles() {
        // Given
        when(tokenOutboundPort.validateJwtToken(validToken)).thenReturn(jwt);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Test User");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(Map.of());
        when(jwt.getIssuedAt()).thenReturn(issuedAt);
        when(jwt.getExpiresAt()).thenReturn(expiresAt);

        // When
        TokenValidationResponse result = tokenValidationUseCase.validateToken(validToken);

        // Then
        assertTrue(result.isValid());
        assertEquals(List.of(), result.getRoles());
    }

    @Test
    void shouldHandleNullRealmAccess() {
        // Given
        when(tokenOutboundPort.validateJwtToken(validToken)).thenReturn(jwt);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Test User");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(null);
        when(jwt.getIssuedAt()).thenReturn(issuedAt);
        when(jwt.getExpiresAt()).thenReturn(expiresAt);

        // When
        TokenValidationResponse result = tokenValidationUseCase.validateToken(validToken);

        // Then
        assertTrue(result.isValid());
        assertEquals(List.of(), result.getRoles());
    }

    @Test
    void shouldHandleExceptionInRoleExtraction() {
        // Given
        when(tokenOutboundPort.validateJwtToken(validToken)).thenReturn(jwt);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Test User");
        when(jwt.getClaimAsMap("realm_access"))
                .thenThrow(new RuntimeException("Error extracting roles"));
        when(jwt.getIssuedAt()).thenReturn(issuedAt);
        when(jwt.getExpiresAt()).thenReturn(expiresAt);

        // When
        TokenValidationResponse result = tokenValidationUseCase.validateToken(validToken);

        // Then
        assertTrue(result.isValid());
        assertEquals(List.of(), result.getRoles());
    }

    @Test
    void shouldHandleNullTimestamps() {
        // Given
        when(tokenOutboundPort.validateJwtToken(validToken)).thenReturn(jwt);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(jwt.getClaimAsString("name")).thenReturn("Test User");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(
                Map.of("roles", List.of("USER"))
        );
        when(jwt.getIssuedAt()).thenReturn(null);
        when(jwt.getExpiresAt()).thenReturn(null);

        // When
        TokenValidationResponse result = tokenValidationUseCase.validateToken(validToken);

        // Then
        assertTrue(result.isValid());
        assertNull(result.getIssuedAt());
        assertNull(result.getExpiresAt());
    }
}