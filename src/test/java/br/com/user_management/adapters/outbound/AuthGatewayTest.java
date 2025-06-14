package br.com.user_management.adapters.outbound;

import br.com.user_management.adapters.config.KeycloakProperties;
import br.com.user_management.adapters.inbound.rest.dto.response.LoginResponse;
import br.com.user_management.core.domain.Login;
import br.com.user_management.core.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthGatewayTest {

    @Mock
    private KeycloakProperties keycloakProperties;

    @Mock
    private RestTemplate restTemplate;

    private AuthGateway authGateway;
    private Login login;

    @BeforeEach
    public void setUp() throws Exception {
        // Configurar propriedades do Keycloak
        when(keycloakProperties.getAuthServerUrl()).thenReturn("http://localhost:8080/auth");
        when(keycloakProperties.getRealm()).thenReturn("test-realm");
        when(keycloakProperties.getClientId()).thenReturn("test-client");
        when(keycloakProperties.getClientSecret()).thenReturn("test-secret");

        // Criar instância do AuthGateway
        authGateway = new AuthGateway(keycloakProperties);

        // Injetar o RestTemplate mockado usando reflection
        injectRestTemplate(authGateway, restTemplate);

        // Criar objeto Login para testes
        login = new Login();
        login.setEmail("test@example.com");
        login.setPassword("password123");
    }

    @Test
    public void shouldAuthenticateSuccessfully() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("access_token", "mock-access-token");
        responseBody.put("refresh_token", "mock-refresh-token");
        responseBody.put("token_type", "Bearer");
        responseBody.put("expires_in", 3600);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        LoginResponse result = authGateway.authenticateWithKeycloak(login);

        // Assert
        assertNotNull(result);
        assertEquals("mock-access-token", result.getAccessToken());
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(Long.valueOf(3600), result.getExpiresIn());

        // Verificar se o RestTemplate foi chamado com os parâmetros corretos
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/auth/realms/test-realm/protocol/openid-connect/token"),
                argThat(this::verifyHttpEntity),
                eq(Map.class)
        );
    }

    @Test
    public void shouldAuthenticateSuccessfullyWithDefaultTokenType() {
        // Arrange - Resposta sem token_type
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("access_token", "mock-access-token");
        responseBody.put("refresh_token", "mock-refresh-token");
        responseBody.put("expires_in", 3600);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        LoginResponse result = authGateway.authenticateWithKeycloak(login);

        // Assert
        assertNotNull(result);
        assertEquals("Bearer", result.getTokenType()); // Deve usar o valor padrão
    }

    @Test
    public void shouldThrowExceptionWhenUnauthorized() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(HttpClientErrorException.Unauthorized.class);

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            authGateway.authenticateWithKeycloak(login);
        });

        assertEquals("E-mail ou senha inválidos", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenBadRequest() {
        // Arrange
        HttpClientErrorException badRequestException =
                new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(badRequestException);

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            authGateway.authenticateWithKeycloak(login);
        });

        assertTrue(exception.getMessage().startsWith("Erro na autenticação:"));
    }

    @Test
    public void shouldThrowExceptionWhenResponseIsNotOk() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            authGateway.authenticateWithKeycloak(login);
        });

        assertEquals("Falha na autenticação", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenResponseBodyIsNull() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            authGateway.authenticateWithKeycloak(login);
        });

        assertEquals("Falha na autenticação", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenGenericExceptionOccurs() {
        // Arrange
        RuntimeException genericException = new RuntimeException("Connection timeout");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(genericException);

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            authGateway.authenticateWithKeycloak(login);
        });

        assertTrue(exception.getMessage().startsWith("Erro interno na autenticação:"));
        assertEquals(genericException, exception.getCause());
    }

    @Test
    public void shouldHandleNumericExpiresInAsInteger() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("access_token", "mock-access-token");
        responseBody.put("refresh_token", "mock-refresh-token");
        responseBody.put("token_type", "Bearer");
        responseBody.put("expires_in", 7200); // Integer ao invés de Long

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        LoginResponse result = authGateway.authenticateWithKeycloak(login);

        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(7200), result.getExpiresIn());
    }

    // Método auxiliar para injetar o RestTemplate mockado usando reflection
    private void injectRestTemplate(AuthGateway authGateway, RestTemplate restTemplate) throws Exception {
        Field restTemplateField = AuthGateway.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(authGateway, restTemplate);
    }

    // Método auxiliar para verificar o HttpEntity
    private boolean verifyHttpEntity(HttpEntity<MultiValueMap<String, String>> httpEntity) {
        MultiValueMap<String, String> body = httpEntity.getBody();
        HttpHeaders headers = httpEntity.getHeaders();

        // Verificar headers
        if (!MediaType.APPLICATION_FORM_URLENCODED.equals(headers.getContentType())) {
            return false;
        }

        // Verificar body
        if (body == null) {
            return false;
        }

        return "password".equals(body.getFirst("grant_type")) &&
                "test-client".equals(body.getFirst("client_id")) &&
                "test-secret".equals(body.getFirst("client_secret")) &&
                "test@example.com".equals(body.getFirst("username")) &&
                "password123".equals(body.getFirst("password"));
    }
}