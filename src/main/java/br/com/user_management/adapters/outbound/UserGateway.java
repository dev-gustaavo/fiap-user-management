package br.com.user_management.adapters.outbound;

import br.com.user_management.adapters.config.KeycloakProperties;
import br.com.user_management.application.ports.outbound.UserOutboundPort;
import br.com.user_management.core.domain.User;
import br.com.user_management.core.exception.UserException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class UserGateway implements UserOutboundPort {

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;
    private final Keycloak keycloakAdmin;

    public UserGateway(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.restTemplate = new RestTemplate();
        this.keycloakAdmin = createKeycloakAdminClient();
    }

    private Keycloak createKeycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloakProperties.getAdmin().getUsername())
                .password(keycloakProperties.getAdmin().getPassword())
                .build();
    }

    public boolean userExists(String email) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(email, true);
            return !users.isEmpty();

        } catch (Exception e) {
            throw new UserException("Erro ao verificar se usuário existe", e);
        }
    }

    public void createUserInKeycloak(User user) {
        try {
            createKeycloakUser(user);
        } catch (Exception e) {
            throw new UserException("Erro ao criar usuário no Keycloak: " + e.getMessage(), e);
        }
    }

    private String createKeycloakUser(User user) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        // Criar representação do usuário
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(user.getEmail());
        userRep.setEmail(user.getEmail());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEnabled(true);
        userRep.setEmailVerified(true);

        // Criar credencial de senha
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(user.getPassword());
        credential.setTemporary(false);
        userRep.setCredentials(Arrays.asList(credential));

        // Criar usuário
        Response response = usersResource.create(userRep);

        if (response.getStatus() != 201) {
            throw new UserException("Falha ao criar usuário no Keycloak. Status: " + response.getStatus());
        }

        // Extrair ID do usuário da URL de resposta
        String location = response.getHeaderString("Location");
        String userId = location.substring(location.lastIndexOf('/') + 1);

        response.close();
        return userId;
    }
}
