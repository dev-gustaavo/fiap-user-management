package br.com.user_management.adapters.inbound.rest;

import br.com.user_management.adapters.inbound.rest.dto.request.UserRequest;
import br.com.user_management.application.mappers.UserMapper;
import br.com.user_management.application.ports.inbound.UserInboundPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserInboundPort userInboundPort;
    private final UserMapper userMapper;

    @PostMapping("/create")
    public ResponseEntity createUser(@RequestBody @Valid UserRequest userRequest) {

        var user = userMapper.toDomain(userRequest);

        userInboundPort.createUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
