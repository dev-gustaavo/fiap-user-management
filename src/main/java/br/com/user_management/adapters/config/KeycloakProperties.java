package br.com.user_management.adapters.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Getter
@Setter
public class KeycloakProperties {

    private String realm;
    private String authServerUrl;
    private String clientId;
    private String clientSecret;
    private Admin admin = new Admin();

    @Getter
    @Setter
    public static class Admin {
        private String username;
        private String password;
    }
}
