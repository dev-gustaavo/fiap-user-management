package br.com.user_management.application.ports.outbound;

import org.springframework.security.oauth2.jwt.Jwt;

public interface TokenOutboundPort {

    Jwt validateJwtToken(String token);
}
