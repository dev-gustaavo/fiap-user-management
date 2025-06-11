package br.com.user_management.adapters.inbound.rest;

import br.com.user_management.adapters.inbound.rest.dto.request.LoginRequest;
import br.com.user_management.adapters.inbound.rest.dto.response.LoginResponse;
import br.com.user_management.application.mappers.UserMapper;
import br.com.user_management.application.ports.inbound.AuthInboundPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthInboundPort authInboundPort;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        var login = userMapper.toLoginDomain(loginRequest);
        var loginResponse = authInboundPort.authenticate(login);
        return ResponseEntity.ok(loginResponse);
    }
}
