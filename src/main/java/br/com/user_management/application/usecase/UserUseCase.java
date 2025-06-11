package br.com.user_management.application.usecase;

import br.com.user_management.application.ports.inbound.UserInboundPort;
import br.com.user_management.application.ports.outbound.UserOutboundPort;
import br.com.user_management.core.domain.User;
import br.com.user_management.core.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUseCase implements UserInboundPort {

    private final UserOutboundPort userOutboundPort;

    @Override
    public void createUser(User user) {
        if (userOutboundPort.userExists(user.getEmail()))
            throw new UserException("User already exists.");

        userOutboundPort.createUserInKeycloak(user);
    }
}
