package br.com.user_management.adapters.inbound.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TokenValidationResponse {

    @JsonProperty("valid")
    private boolean valid;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @JsonProperty("issued_at")
    private LocalDateTime issuedAt;

    public TokenValidationResponse() {}

    public TokenValidationResponse(boolean valid, String username, String email, String name,
                                   List<String> roles, LocalDateTime expiresAt, LocalDateTime issuedAt) {
        this.valid = valid;
        this.username = username;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.expiresAt = expiresAt;
        this.issuedAt = issuedAt;
    }

    public TokenValidationResponse(boolean valid) {
        this.valid = valid;
    }
}
