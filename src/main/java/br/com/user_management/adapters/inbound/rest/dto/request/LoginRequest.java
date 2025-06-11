package br.com.user_management.adapters.inbound.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotNull(message = "E-mail é obrigatório")
    @NotBlank(message = "E-mail não pode estar vazio")
    @Email(message = "E-mail deve ter um formato válido")
    private String email;

    @NotNull(message = "Senha é obrigatória")
    @NotBlank(message = "Senha não pode estar vazia")
    private String password;
}
