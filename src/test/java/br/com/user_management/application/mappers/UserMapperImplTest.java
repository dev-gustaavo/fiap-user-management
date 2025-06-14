package br.com.user_management.application.mappers;

import br.com.user_management.adapters.inbound.rest.dto.request.LoginRequest;
import br.com.user_management.adapters.inbound.rest.dto.request.UserRequest;
import br.com.user_management.core.domain.Login;
import br.com.user_management.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperImplTest {

    private UserMapperImpl userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
    }

    // Testes para o método toDomain(UserRequest)

    @Test
    void shouldConvertUserRequestToDomainSuccessfully() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setPassword("password123");

        // When
        User result = userMapper.toDomain(userRequest);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
    }

    @Test
    void shouldReturnNullWhenUserRequestIsNull() {
        // Given
        UserRequest userRequest = null;

        // When
        User result = userMapper.toDomain(userRequest);

        // Then
        assertNull(result);
    }

    @Test
    void shouldHandleUserRequestWithNullFields() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName(null);
        userRequest.setLastName(null);
        userRequest.setEmail(null);
        userRequest.setPassword(null);

        // When
        User result = userMapper.toDomain(userRequest);

        // Then
        assertNotNull(result);
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getEmail());
        assertNull(result.getPassword());
    }

    @Test
    void shouldHandleUserRequestWithEmptyStrings() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("");
        userRequest.setLastName("");
        userRequest.setEmail("");
        userRequest.setPassword("");

        // When
        User result = userMapper.toDomain(userRequest);

        // Then
        assertNotNull(result);
        assertEquals("", result.getFirstName());
        assertEquals("", result.getLastName());
        assertEquals("", result.getEmail());
        assertEquals("", result.getPassword());
    }

    @Test
    void shouldHandleUserRequestWithPartialData() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("Jane");
        userRequest.setLastName(null);
        userRequest.setEmail("jane@example.com");
        userRequest.setPassword(null);

        // When
        User result = userMapper.toDomain(userRequest);

        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertNull(result.getLastName());
        assertEquals("jane@example.com", result.getEmail());
        assertNull(result.getPassword());
    }

    @Test
    void shouldHandleUserRequestWithSpecialCharacters() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("José");
        userRequest.setLastName("Silva-Santos");
        userRequest.setEmail("josé.silva+test@example.com.br");
        userRequest.setPassword("P@ssw0rd!#$");

        // When
        User result = userMapper.toDomain(userRequest);

        // Then
        assertNotNull(result);
        assertEquals("José", result.getFirstName());
        assertEquals("Silva-Santos", result.getLastName());
        assertEquals("josé.silva+test@example.com.br", result.getEmail());
        assertEquals("P@ssw0rd!#$", result.getPassword());
    }

    @Test
    void shouldHandleUserRequestWithLongStrings() {
        // Given
        String longFirstName = "A".repeat(100);
        String longLastName = "B".repeat(100);
        String longEmail = "test@" + "domain".repeat(20) + ".com";
        String longPassword = "password".repeat(50);

        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName(longFirstName);
        userRequest.setLastName(longLastName);
        userRequest.setEmail(longEmail);
        userRequest.setPassword(longPassword);

        // When
        User result = userMapper.toDomain(userRequest);

        // Then
        assertNotNull(result);
        assertEquals(longFirstName, result.getFirstName());
        assertEquals(longLastName, result.getLastName());
        assertEquals(longEmail, result.getEmail());
        assertEquals(longPassword, result.getPassword());
    }

    // Testes para o método toLoginDomain(LoginRequest)

    @Test
    void shouldConvertLoginRequestToDomainSuccessfully() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("mypassword");

        // When
        Login result = userMapper.toLoginDomain(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        assertEquals("mypassword", result.getPassword());
    }

    @Test
    void shouldReturnNullWhenLoginRequestIsNull() {
        // Given
        LoginRequest loginRequest = null;

        // When
        Login result = userMapper.toLoginDomain(loginRequest);

        // Then
        assertNull(result);
    }

    @Test
    void shouldHandleLoginRequestWithNullFields() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(null);
        loginRequest.setPassword(null);

        // When
        Login result = userMapper.toLoginDomain(loginRequest);

        // Then
        assertNotNull(result);
        assertNull(result.getEmail());
        assertNull(result.getPassword());
    }

    @Test
    void shouldHandleLoginRequestWithEmptyStrings() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("");
        loginRequest.setPassword("");

        // When
        Login result = userMapper.toLoginDomain(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("", result.getEmail());
        assertEquals("", result.getPassword());
    }

    @Test
    void shouldHandleLoginRequestWithPartialData() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("partial@example.com");
        loginRequest.setPassword(null);

        // When
        Login result = userMapper.toLoginDomain(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("partial@example.com", result.getEmail());
        assertNull(result.getPassword());
    }

    @Test
    void shouldHandleLoginRequestWithSpecialCharacters() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test+special@domain-name.co.uk");
        loginRequest.setPassword("Special!@#$%Password123");

        // When
        Login result = userMapper.toLoginDomain(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("test+special@domain-name.co.uk", result.getEmail());
        assertEquals("Special!@#$%Password123", result.getPassword());
    }

    @Test
    void shouldHandleLoginRequestWithWhitespace() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("  user@example.com  ");
        loginRequest.setPassword("  password123  ");

        // When
        Login result = userMapper.toLoginDomain(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("  user@example.com  ", result.getEmail());
        assertEquals("  password123  ", result.getPassword());
    }

    // Testes de integração/comportamento

    @Test
    void shouldCreateIndependentObjects() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("Test");
        userRequest.setLastName("User");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("testpass");

        // When
        User result1 = userMapper.toDomain(userRequest);
        User result2 = userMapper.toDomain(userRequest);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2); // Diferentes instâncias
        assertEquals(result1.getFirstName(), result2.getFirstName());
        assertEquals(result1.getLastName(), result2.getLastName());
        assertEquals(result1.getEmail(), result2.getEmail());
        assertEquals(result1.getPassword(), result2.getPassword());
    }

    @Test
    void shouldCreateIndependentLoginObjects() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("testpass");

        // When
        Login result1 = userMapper.toLoginDomain(loginRequest);
        Login result2 = userMapper.toLoginDomain(loginRequest);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2); // Diferentes instâncias
        assertEquals(result1.getEmail(), result2.getEmail());
        assertEquals(result1.getPassword(), result2.getPassword());
    }

    @Test
    void shouldNotModifyOriginalRequest() {
        // Given
        UserRequest originalRequest = new UserRequest();
        originalRequest.setFirstName("Original");
        originalRequest.setLastName("User");
        originalRequest.setEmail("original@example.com");
        originalRequest.setPassword("originalpass");

        // When
        User mappedUser = userMapper.toDomain(originalRequest);
        mappedUser.setFirstName("Modified");

        // Then
        assertEquals("Original", originalRequest.getFirstName()); // Original não foi modificado
        assertEquals("Modified", mappedUser.getFirstName()); // Mapeado foi modificado
    }

    @Test
    void shouldNotModifyOriginalLoginRequest() {
        // Given
        LoginRequest originalRequest = new LoginRequest();
        originalRequest.setEmail("original@example.com");
        originalRequest.setPassword("originalpass");

        // When
        Login mappedLogin = userMapper.toLoginDomain(originalRequest);
        mappedLogin.setEmail("modified@example.com");

        // Then
        assertEquals("original@example.com", originalRequest.getEmail()); // Original não foi modificado
        assertEquals("modified@example.com", mappedLogin.getEmail()); // Mapeado foi modificado
    }
}