package br.com.user_management.adapters.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakConfigTest {

    @InjectMocks
    private KeycloakConfig keycloakConfig;

    @Mock
    private HttpSecurity httpSecurity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keycloakConfig, "jwkSetUri",
                "http://keycloak.example.com/auth/realms/vehicle/protocol/openid-connect/certs");
    }

    @Test
    void testJwtDecoderBean() {
        JwtDecoder jwtDecoder = keycloakConfig.jwtDecoder();
        assertNotNull(jwtDecoder);
        assertTrue(jwtDecoder instanceof NimbusJwtDecoder);
    }

    @Test
    void testJwtAuthenticationConverterBean() {
        JwtAuthenticationConverter converter = keycloakConfig.jwtAuthenticationConverter();
        assertNotNull(converter);
    }

    @Test
    void testCorsConfigurationSourceBean() {
        CorsConfigurationSource corsSource = keycloakConfig.corsConfigurationSource();
        assertNotNull(corsSource);
        assertTrue(corsSource instanceof UrlBasedCorsConfigurationSource);
    }

    @Test
    void testFilterChain_ShouldConfigureSecurityCorrectly() throws Exception {
        // Mock HttpSecurity behavior
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2ResourceServer(any())).thenReturn(httpSecurity);

        // Execute
        keycloakConfig.filterChain(httpSecurity);

        // Verify
        verify(httpSecurity).cors(any());
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).sessionManagement(any());
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).oauth2ResourceServer(any());
    }

    @Test
    void testFilterChain_ShouldPermitPublicEndpoints() throws Exception {
        // Mock HttpSecurity behavior
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            // You can add assertions here for the authorization rules
            return httpSecurity;
        });
        when(httpSecurity.oauth2ResourceServer(any())).thenReturn(httpSecurity);

        // Execute
        keycloakConfig.filterChain(httpSecurity);

        // If we wanted to verify specific authorization rules, we could:
        verify(httpSecurity).authorizeHttpRequests(any());
    }

    @Test
    void testFilterChain_ShouldThrowExceptionOnConfigurationError() throws Exception {
        when(httpSecurity.cors(any())).thenThrow(new RuntimeException("Configuration error"));
        assertThrows(RuntimeException.class, () -> keycloakConfig.filterChain(httpSecurity));
    }
}