package br.com.user_management.adapters.inbound;

import br.com.user_management.adapters.inbound.rest.TokenController;
import br.com.user_management.adapters.inbound.rest.dto.response.TokenValidationResponse;
import br.com.user_management.application.ports.inbound.TokenInboundPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    @Mock
    private TokenInboundPort tokenInboundPort;

    @InjectMocks
    private TokenController tokenController;

    @Test
    void validateToken_ShouldReturnOkWhenTokenIsValid() {
        // Arrange
        String validToken = "Bearer valid.token.here";
        TokenValidationResponse validResponse = new TokenValidationResponse(true);

        when(tokenInboundPort.validateToken(validToken)).thenReturn(validResponse);

        // Act
        ResponseEntity<TokenValidationResponse> response =
                tokenController.validateToken(validToken);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(validResponse, response.getBody());
        assertTrue(response.getBody().isValid());
        verify(tokenInboundPort).validateToken(validToken);
    }

    @Test
    void validateToken_ShouldReturnUnauthorizedWhenTokenIsInvalid() {
        // Arrange
        String invalidToken = "Bearer invalid.token.here";
        TokenValidationResponse invalidResponse = new TokenValidationResponse(false);

        when(tokenInboundPort.validateToken(invalidToken)).thenReturn(invalidResponse);

        // Act
        ResponseEntity<TokenValidationResponse> response =
                tokenController.validateToken(invalidToken);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals(invalidResponse, response.getBody());
        assertFalse(response.getBody().isValid());
        verify(tokenInboundPort).validateToken(invalidToken);
    }

    @Test
    void validateToken_ShouldReturnBadRequestWhenHeaderIsNull() {
        // Act
        ResponseEntity<TokenValidationResponse> response =
                tokenController.validateToken(null);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isValid());
        verify(tokenInboundPort, never()).validateToken(any());
    }

    @Test
    void validateToken_ShouldReturnBadRequestWhenHeaderIsEmpty() {
        // Act
        ResponseEntity<TokenValidationResponse> response =
                tokenController.validateToken("");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isValid());
        verify(tokenInboundPort, never()).validateToken(any());
    }

    @Test
    void validateToken_ShouldReturnBadRequestWhenHeaderIsBlank() {
        // Act
        ResponseEntity<TokenValidationResponse> response =
                tokenController.validateToken("   ");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isValid());
        verify(tokenInboundPort, never()).validateToken(any());
    }
}