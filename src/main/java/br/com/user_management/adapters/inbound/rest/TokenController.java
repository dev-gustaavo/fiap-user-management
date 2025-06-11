package br.com.user_management.adapters.inbound.rest;

import br.com.user_management.adapters.inbound.rest.dto.response.TokenValidationResponse;
import br.com.user_management.application.ports.inbound.TokenInboundPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/token")
@CrossOrigin
public class TokenController {

    private final TokenInboundPort tokenInboundPort;

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new TokenValidationResponse(false));
        }

        TokenValidationResponse response = tokenInboundPort.validateToken(authorizationHeader);

        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }
}
