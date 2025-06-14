package br.com.user_management.application.usecases;

import br.com.user_management.application.ports.outbound.UserOutboundPort;
import br.com.user_management.application.usecase.UserUseCase;
import br.com.user_management.core.domain.User;
import br.com.user_management.core.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private UserOutboundPort userOutboundPort;

    @InjectMocks
    private UserUseCase userUseCase;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test User");
        user.setPassword("password123");
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        when(userOutboundPort.userExists(anyString())).thenReturn(false);
        doNothing().when(userOutboundPort).createUserInKeycloak(any(User.class));

        // When
        assertDoesNotThrow(() -> userUseCase.createUser(user));

        // Then
        verify(userOutboundPort, times(1)).userExists(user.getEmail());
        verify(userOutboundPort, times(1)).createUserInKeycloak(user);
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExists() {
        // Given
        when(userOutboundPort.userExists(user.getEmail())).thenReturn(true);

        // When & Then
        UserException exception = assertThrows(UserException.class,
                () -> userUseCase.createUser(user));

        assertEquals("User already exists.", exception.getMessage());
        verify(userOutboundPort, times(1)).userExists(user.getEmail());
        verify(userOutboundPort, never()).createUserInKeycloak(any(User.class));
    }

    @Test
    void shouldCheckUserExistenceWithCorrectEmail() {
        // Given
        when(userOutboundPort.userExists(user.getEmail())).thenReturn(false);
        doNothing().when(userOutboundPort).createUserInKeycloak(user);

        // When
        userUseCase.createUser(user);

        // Then
        verify(userOutboundPort).userExists(user.getEmail());
    }

    @Test
    void shouldCallCreateUserInKeycloakWithCorrectUser() {
        // Given
        when(userOutboundPort.userExists(anyString())).thenReturn(false);
        doNothing().when(userOutboundPort).createUserInKeycloak(user);

        // When
        userUseCase.createUser(user);

        // Then
        verify(userOutboundPort).createUserInKeycloak(user);
    }

    @Test
    void shouldPropagateExceptionFromOutboundPort() {
        // Given
        when(userOutboundPort.userExists(anyString())).thenReturn(false);
        RuntimeException exception = new RuntimeException("Keycloak error");
        doThrow(exception).when(userOutboundPort).createUserInKeycloak(any(User.class));

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> userUseCase.createUser(user));

        assertEquals("Keycloak error", thrown.getMessage());
        verify(userOutboundPort, times(1)).userExists(user.getEmail());
        verify(userOutboundPort, times(1)).createUserInKeycloak(user);
    }
}