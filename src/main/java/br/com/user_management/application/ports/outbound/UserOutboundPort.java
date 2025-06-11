package br.com.user_management.application.ports.outbound;

import br.com.user_management.core.domain.User;

public interface UserOutboundPort {

    void createUserInKeycloak(User user);

    boolean userExists(String email);
}
