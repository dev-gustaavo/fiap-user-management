package br.com.user_management.adapters.inbound;

import br.com.user_management.adapters.inbound.rest.UserController;
import br.com.user_management.adapters.inbound.rest.dto.request.UserRequest;
import br.com.user_management.application.mappers.UserMapper;
import br.com.user_management.application.ports.inbound.UserInboundPort;
import br.com.user_management.core.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserInboundPort userInboundPort;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser_ShouldReturnCreatedStatusWhenSuccessful() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setFirstName("testuser");
        request.setPassword("securePassword123");
        request.setEmail("test@example.com");

        User mappedUser = new User();
        mappedUser.setFirstName("testuser");

        when(userMapper.toDomain(request)).thenReturn(mappedUser);

        // Act
        ResponseEntity<?> response = userController.createUser(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userMapper).toDomain(request);
        verify(userInboundPort).createUser(mappedUser);
    }

    @Test
    void createUser_ShouldMapRequestToDomainCorrectly() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setFirstName("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        User expectedUser = new User();
        expectedUser.setFirstName("newuser");
        expectedUser.setEmail("new@example.com");

        when(userMapper.toDomain(request)).thenReturn(expectedUser);

        // Act
        ResponseEntity<?> response = userController.createUser(request);

        // Assert
        verify(userMapper).toDomain(request);
        verify(userInboundPort).createUser(expectedUser);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createUser_ShouldCallPortWithMappedUser() {
        // Arrange
        UserRequest request = new UserRequest();
        User mappedUser = new User();

        when(userMapper.toDomain(request)).thenReturn(mappedUser);

        // Act
        userController.createUser(request);

        // Assert
        verify(userInboundPort).createUser(mappedUser);
    }
}