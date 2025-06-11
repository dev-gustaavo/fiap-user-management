package br.com.user_management.adapters.outbound;

import br.com.user_management.application.ports.outbound.TokenOutboundPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class KeycloakTokenGateway implements TokenOutboundPort {

    private final JwtDecoder jwtDecoder;

    @Autowired
    public KeycloakTokenGateway(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Jwt validateJwtToken(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            return null;
        }
    }
}
