package br.com.user_management.application.usecase;

import br.com.user_management.adapters.inbound.rest.dto.response.TokenValidationResponse;
import br.com.user_management.application.ports.inbound.TokenInboundPort;
import br.com.user_management.application.ports.outbound.TokenOutboundPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenValidationUseCase implements TokenInboundPort {

    private final TokenOutboundPort tokenOutboundPort;

    @Override
    public TokenValidationResponse validateToken(String token) {
        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            Jwt jwt = tokenOutboundPort.validateJwtToken(cleanToken);

            if (jwt != null) {
                return buildValidTokenResponse(jwt);
            }

            return new TokenValidationResponse(false);
        } catch (Exception e) {
            return new TokenValidationResponse(false);
        }
    }

    private TokenValidationResponse buildValidTokenResponse(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");

        List<String> roles = extractRoles(jwt);

        LocalDateTime issuedAt = convertToLocalDateTime(jwt.getIssuedAt());
        LocalDateTime expiresAt = convertToLocalDateTime(jwt.getExpiresAt());

        return new TokenValidationResponse(
                true,
                username,
                email,
                name,
                roles,
                expiresAt,
                issuedAt
        );
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                return (List<String>) realmAccess.get("roles");
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private LocalDateTime convertToLocalDateTime(Instant instant) {
        return instant != null ?
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
