package br.com.user_management.application.ports.inbound;

import br.com.user_management.adapters.inbound.rest.dto.response.LoginResponse;
import br.com.user_management.core.domain.Login;

public interface AuthInboundPort {

    LoginResponse authenticate(Login login);
}
