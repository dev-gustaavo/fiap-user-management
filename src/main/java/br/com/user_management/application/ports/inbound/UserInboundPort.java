package br.com.user_management.application.ports.inbound;

import br.com.user_management.core.domain.User;

public interface UserInboundPort {

    void createUser(User user);
}
