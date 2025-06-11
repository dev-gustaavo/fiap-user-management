package br.com.user_management.application.usecase;

import br.com.user_management.adapters.inbound.rest.dto.response.LoginResponse;
import br.com.user_management.application.ports.inbound.AuthInboundPort;
import br.com.user_management.application.ports.outbound.AuthOutboundPort;
import br.com.user_management.core.domain.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUseCase implements AuthInboundPort {

    private final AuthOutboundPort authOutboundPort;

    @Override
    public LoginResponse authenticate(Login login) {
        return authOutboundPort.authenticateWithKeycloak(login);
    }
}
