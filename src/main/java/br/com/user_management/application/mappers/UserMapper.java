package br.com.user_management.application.mappers;

import br.com.user_management.adapters.inbound.rest.dto.request.LoginRequest;
import br.com.user_management.adapters.inbound.rest.dto.request.UserRequest;
import br.com.user_management.core.domain.Login;
import br.com.user_management.core.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toDomain(UserRequest userRequest);

    Login toLoginDomain(LoginRequest loginRequest);
}
