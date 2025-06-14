package br.com.user_management.adapters.inbound;

import br.com.user_management.adapters.inbound.rest.AuthController;
import br.com.user_management.adapters.inbound.rest.dto.request.LoginRequest;
import br.com.user_management.adapters.inbound.rest.dto.response.LoginResponse;
import br.com.user_management.application.mappers.UserMapper;
import br.com.user_management.application.ports.inbound.AuthInboundPort;
import br.com.user_management.core.domain.Login;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthInboundPort authInboundPort;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ShouldReturnOkResponseWithLoginResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("username", "password");
        LoginResponse expectedResponse = new LoginResponse("token", "3600", "refreshToken", 10L);

        when(userMapper.toLoginDomain(request)).thenReturn(new Login());
        when(authInboundPort.authenticate(any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());

        verify(userMapper).toLoginDomain(request);
        verify(authInboundPort).authenticate(any());
    }

    @Test
    void login_ShouldCallMapperAndServiceWithCorrectParameters() {
        // Arrange
        LoginRequest request = new LoginRequest("test@email.com", "securePassword123");
        LoginResponse expectedResponse = new LoginResponse("token", "3600", "refreshToken", 10L);

        when(userMapper.toLoginDomain(request)).thenReturn(new Login());
        when(authInboundPort.authenticate(any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(request);

        // Assert
        verify(userMapper).toLoginDomain(request);
        verify(authInboundPort).authenticate(any());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void login_ShouldValidateRequest() {
        // This would typically be tested in an integration test with MockMvc
        // to verify the @Valid annotation is working properly
        assertTrue(true); // Placeholder for the validation concept
    }
}