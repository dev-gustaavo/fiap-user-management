package br.com.user_management.adapters.outbound;

import br.com.user_management.adapters.config.KeycloakProperties;
import br.com.user_management.core.domain.User;
import br.com.user_management.core.exception.UserException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserGatewayTest {

    @Mock
    private KeycloakProperties keycloakProperties;

    @Mock
    private KeycloakProperties.Admin adminProperties;

    @Mock
    private Keycloak keycloakAdmin;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private Response response;

    @Mock
    private RestTemplate restTemplate;

    private UserGateway userGateway;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        // Configurar propriedades do Keycloak
        when(keycloakProperties.getAuthServerUrl()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getRealm()).thenReturn("test-realm");
        when(keycloakProperties.getAdmin()).thenReturn(adminProperties);
        when(adminProperties.getUsername()).thenReturn("admin");
        when(adminProperties.getPassword()).thenReturn("admin-password");

        // Configurar mocks do Keycloak
        when(keycloakAdmin.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        // Criar instância do UserGateway com mock do Keycloak
        userGateway = new UserGateway(keycloakProperties);
        injectKeycloakAdmin(userGateway, keycloakAdmin);

        // Criar objeto User para testes
        user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
    }

    @Test
    public void shouldReturnTrueWhenUserExists() {
        // Arrange
        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setEmail("test@example.com");
        existingUser.setUsername("test@example.com");

        when(usersResource.search("test@example.com", true))
                .thenReturn(Arrays.asList(existingUser));

        // Act
        boolean result = userGateway.userExists("test@example.com");

        // Assert
        assertTrue(result);
        verify(usersResource).search("test@example.com", true);
    }

    @Test
    public void shouldReturnFalseWhenUserDoesNotExist() {
        // Arrange
        when(usersResource.search("nonexistent@example.com", true))
                .thenReturn(Collections.emptyList());

        // Act
        boolean result = userGateway.userExists("nonexistent@example.com");

        // Assert
        assertFalse(result);
        verify(usersResource).search("nonexistent@example.com", true);
    }

    @Test
    public void shouldThrowExceptionWhenUserExistsCheckFails() {
        // Arrange
        when(usersResource.search("error@example.com", true))
                .thenThrow(new RuntimeException("Keycloak connection error"));

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            userGateway.userExists("error@example.com");
        });

        assertEquals("Erro ao verificar se usuário existe", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    public void shouldCreateUserInKeycloakSuccessfully() {
        // Arrange
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location"))
                .thenReturn("http://localhost:8080/auth/admin/realms/test-realm/users/12345");
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        // Act
        assertDoesNotThrow(() -> {
            userGateway.createUserInKeycloak(user);
        });

        // Assert
        verify(usersResource).create(argThat(userRep -> {
            return "test@example.com".equals(userRep.getEmail()) &&
                    "test@example.com".equals(userRep.getUsername()) &&
                    "Test".equals(userRep.getFirstName()) &&
                    "User".equals(userRep.getLastName()) &&
                    userRep.isEnabled() &&
                    userRep.isEmailVerified() &&
                    userRep.getCredentials().size() == 1;
        }));
        verify(response).close();
    }

    @Test
    public void shouldThrowExceptionWhenCreateUserFails() {
        // Arrange
        when(response.getStatus()).thenReturn(400);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            userGateway.createUserInKeycloak(user);
        });

        assertTrue(exception.getMessage().contains("Falha ao criar usuário no Keycloak"));
    }

    @Test
    public void shouldThrowExceptionWhenCreateUserThrowsException() {
        // Arrange
        when(usersResource.create(any(UserRepresentation.class)))
                .thenThrow(new RuntimeException("Keycloak error"));

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            userGateway.createUserInKeycloak(user);
        });

        assertTrue(exception.getMessage().startsWith("Erro ao criar usuário no Keycloak:"));
        assertNotNull(exception.getCause());
    }

    @Test
    public void shouldCreateUserWithCorrectCredentials() {
        // Arrange
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location"))
                .thenReturn("http://localhost:8080/auth/admin/realms/test-realm/users/67890");
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        // Act
        userGateway.createUserInKeycloak(user);

        // Assert
        verify(usersResource).create(argThat(userRep -> {
            if (userRep.getCredentials() == null || userRep.getCredentials().isEmpty()) {
                return false;
            }
            var credential = userRep.getCredentials().get(0);
            return "password".equals(credential.getType()) &&
                    "password123".equals(credential.getValue()) &&
                    !credential.isTemporary();
        }));
    }

    @Test
    public void shouldCreateUserWithCorrectUserRepresentation() {
        // Arrange
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location"))
                .thenReturn("http://localhost:8080/auth/admin/realms/test-realm/users/99999");
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        // Act
        userGateway.createUserInKeycloak(user);

        // Assert
        verify(usersResource).create(argThat(userRep -> {
            return userRep.getUsername().equals("test@example.com") &&
                    userRep.getEmail().equals("test@example.com") &&
                    userRep.getFirstName().equals("Test") &&
                    userRep.getLastName().equals("User") &&
                    userRep.isEnabled() &&
                    userRep.isEmailVerified();
        }));
    }

    @Test
    public void shouldHandleNullUserFields() {
        // Arrange
        User userWithNulls = new User();
        userWithNulls.setEmail("null@example.com");
        userWithNulls.setPassword("password");
        // firstName e lastName são null

        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location"))
                .thenReturn("http://localhost:8080/auth/admin/realms/test-realm/users/11111");
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        // Act
        assertDoesNotThrow(() -> {
            userGateway.createUserInKeycloak(userWithNulls);
        });

        // Assert
        verify(usersResource).create(argThat(userRep -> {
            return "null@example.com".equals(userRep.getEmail()) &&
                    userRep.getFirstName() == null &&
                    userRep.getLastName() == null;
        }));
    }

    @Test
    public void shouldReturnFalseWhenUserSearchReturnsNull() {
        // Arrange
        when(usersResource.search("test@example.com", true)).thenReturn(null);

        // Act
        UserException exception = assertThrows(UserException.class, () -> {
            userGateway.userExists("test@example.com");
        });

        // Assert
        assertEquals("Erro ao verificar se usuário existe", exception.getMessage());
    }

    // Método auxiliar para injetar o Keycloak mockado usando reflection
    private void injectKeycloakAdmin(UserGateway userGateway, Keycloak keycloakAdmin) throws Exception {
        Field keycloakField = UserGateway.class.getDeclaredField("keycloakAdmin");
        keycloakField.setAccessible(true);
        keycloakField.set(userGateway, keycloakAdmin);
    }
}