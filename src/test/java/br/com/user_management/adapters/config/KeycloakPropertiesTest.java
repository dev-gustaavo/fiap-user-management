package br.com.user_management.adapters.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KeycloakPropertiesTest {

    @Test
    void testKeycloakProperties() {
        // Arrange
        KeycloakProperties properties = new KeycloakProperties();
        KeycloakProperties.Admin admin = new KeycloakProperties.Admin();

        // Act
        properties.setRealm("user-management-realm");
        properties.setAuthServerUrl("http://keycloak:8080/auth");
        properties.setClientId("user-management-client");
        properties.setClientSecret("secret123");

        admin.setUsername("admin-user");
        admin.setPassword("admin-password");
        properties.setAdmin(admin);

        // Assert
        assertEquals("user-management-realm", properties.getRealm());
        assertEquals("http://keycloak:8080/auth", properties.getAuthServerUrl());
        assertEquals("user-management-client", properties.getClientId());
        assertEquals("secret123", properties.getClientSecret());

        assertNotNull(properties.getAdmin());
        assertEquals("admin-user", properties.getAdmin().getUsername());
        assertEquals("admin-password", properties.getAdmin().getPassword());
    }

    @Test
    void testAdminProperties() {
        // Arrange
        KeycloakProperties.Admin admin = new KeycloakProperties.Admin();

        // Act
        admin.setUsername("test-admin");
        admin.setPassword("test-password");

        // Assert
        assertEquals("test-admin", admin.getUsername());
        assertEquals("test-password", admin.getPassword());
    }

    @Test
    void testDefaultValues() {
        // Arrange
        KeycloakProperties properties = new KeycloakProperties();

        // Act & Assert
        assertNull(properties.getRealm());
        assertNull(properties.getAuthServerUrl());
        assertNull(properties.getClientId());
        assertNull(properties.getClientSecret());
        assertNotNull(properties.getAdmin()); // Admin object is initialized
        assertNull(properties.getAdmin().getUsername());
        assertNull(properties.getAdmin().getPassword());
    }
}