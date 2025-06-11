package br.com.user_management.application.ports.inbound;

import br.com.user_management.adapters.inbound.rest.dto.response.TokenValidationResponse;

public interface TokenInboundPort {

    TokenValidationResponse validateToken(String token);
}
