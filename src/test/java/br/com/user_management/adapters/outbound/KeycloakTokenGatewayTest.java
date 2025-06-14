package br.com.user_management.adapters.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeycloakTokenGatewayTest {

    @Mock
    private JwtDecoder jwtDecoder;

    private KeycloakTokenGateway keycloakTokenGateway;

    @BeforeEach
    public void setUp() {
        keycloakTokenGateway = new KeycloakTokenGateway(jwtDecoder);
    }

    @Test
    public void shouldValidateJwtTokenSuccessfully() {
        // Arrange
        String token = "valid.jwt.token";
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("email", "test@example.com");
        claims.put("preferred_username", "testuser");
        claims.put("iat", 1640995200L);
        claims.put("exp", 1640998800L);

        Jwt mockJwt = new Jwt(
                token,
                Instant.ofEpochSecond(1640995200L),
                Instant.ofEpochSecond(1640998800L),
                headers,
                claims
        );

        when(jwtDecoder.decode(token)).thenReturn(mockJwt);

        // Act
        Jwt result = keycloakTokenGateway.validateJwtToken(token);

        // Assert
        assertNotNull(result);
        assertEquals(token, result.getTokenValue());
        assertEquals("user123", result.getSubject());
        assertEquals("test@example.com", result.getClaimAsString("email"));
        assertEquals("testuser", result.getClaimAsString("preferred_username"));

        verify(jwtDecoder).decode(token);
    }

    @Test
    public void shouldReturnNullWhenJwtIsInvalid() {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        when(jwtDecoder.decode(invalidToken)).thenThrow(new JwtException("Invalid JWT"));

        // Act
        Jwt result = keycloakTokenGateway.validateJwtToken(invalidToken);

        // Assert
        assertNull(result);
        verify(jwtDecoder).decode(invalidToken);
    }

    @Test
    public void shouldReturnNullWhenJwtIsExpired() {
        // Arrange
        String expiredToken = "expired.jwt.token";
        when(jwtDecoder.decode(expiredToken)).thenThrow(new JwtException("JWT expired"));

        // Act
        Jwt result = keycloakTokenGateway.validateJwtToken(expiredToken);

        // Assert
        assertNull(result);
        verify(jwtDecoder).decode(expiredToken);
    }

    @Test
    public void shouldReturnNullWhenJwtSignatureIsInvalid() {
        // Arrange
        String tokenWithInvalidSignature = "token.with.invalid.signature";
        when(jwtDecoder.decode(tokenWithInvalidSignature))
                .thenThrow(new JwtException("Invalid signature"));

        // Act
        Jwt result = keycloakTokenGateway.validateJwtToken(tokenWithInvalidSignature);

        // Assert
        assertNull(result);
        verify(jwtDecoder).decode(tokenWithInvalidSignature);
    }

    @Test
    public void shouldReturnNullWhenJwtIsMalformed() {
        // Arrange
        String malformedToken = "malformed.token";
        when(jwtDecoder.decode(malformedToken))
                .thenThrow(new JwtException("Malformed JWT"));

        // Act
        Jwt result = keycloakTokenGateway.validateJwtToken(malformedToken);

        // Assert
        assertNull(result);
        verify(jwtDecoder).decode(malformedToken);
    }

    @Test
    public void shouldHandleNullToken() {
        // Arrange
        String nullToken = null;
        when(jwtDecoder.decode(nullToken))
                .thenThrow(new JwtException("Token cannot be null"));

        // Act
        Jwt result = keycloakTokenGateway.validateJwtToken(nullToken);

        // Assert
        assertNull(result);
        verify(jwtDecoder).decode(nullToken);
    }

    @Test
    public void shouldHandleEmptyToken() {
        // Arrange
        String emptyToken = "";
        when(jwtDecoder.decode(emptyToken))
                .thenThrow(new JwtException("Token cannot be empty"));

        // Act
        Jwt result = keycloakTokenGateway.validateJwtToken(emptyToken);

        // Assert
        assertNull(result);
        verify(jwtDecoder).decode(emptyToken);
    }

    @Test
    public void shouldValidateTokenWithMinimalClaims() {
        // Arrange
        String token = "minimal.jwt.token";
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user456");

        Jwt mockJwt = new Jwt(
                token,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );

        when(jwtDecoder.decode(token)).thenReturn(mockJwt);

        // Act
        Jwt result = keycloakTokenGateway.validateJwtToken(token);

        // Assert
        assertNotNull(result);
        assertEquals("user456", result.getSubject());
        verify(jwtDecoder).decode(token);
    }
}