package br.com.user_management.adapters.outbound;

import br.com.user_management.adapters.config.KeycloakProperties;
import br.com.user_management.adapters.inbound.rest.dto.response.LoginResponse;
import br.com.user_management.application.ports.outbound.AuthOutboundPort;
import br.com.user_management.core.domain.Login;
import br.com.user_management.core.exception.UserException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AuthGateway implements AuthOutboundPort {

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;

    public AuthGateway(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public LoginResponse authenticateWithKeycloak(Login login) {
        try {
            String tokenUrl = keycloakProperties.getAuthServerUrl() +
                    "/realms/" + keycloakProperties.getRealm() +
                    "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", keycloakProperties.getClientId());
            body.add("client_secret", keycloakProperties.getClientSecret());
            body.add("username", login.getEmail());
            body.add("password", login.getPassword());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();

                return new LoginResponse(
                        (String) tokenData.get("access_token"),
                        (String) tokenData.get("refresh_token"),
                        (String) tokenData.getOrDefault("token_type", "Bearer"),
                        ((Number) tokenData.get("expires_in")).longValue()
                );
            } else {
                throw new UserException("Falha na autenticação");
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UserException("E-mail ou senha inválidos");
        } catch (HttpClientErrorException e) {
            throw new UserException("Erro na autenticação: " + e.getMessage());
        } catch (UserException e) {
            throw new UserException("Falha na autenticação");
        } catch (Exception e) {
            throw new UserException("Erro interno na autenticação: " + e.getMessage(), e);
        }
    }
}
