package br.com.user_management.application.ports.outbound;

import br.com.user_management.adapters.inbound.rest.dto.response.LoginResponse;
import br.com.user_management.core.domain.Login;

public interface AuthOutboundPort {

    LoginResponse authenticateWithKeycloak(Login login);
}
