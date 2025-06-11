package br.com.user_management.adapters.inbound.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class UserRequest {

    @NotNull(message = "Nome é obrigatório")
    @NotBlank(message = "Nome não pode estar vazio")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String firstName;

    @NotNull(message = "Último Nome é obrigatório")
    @NotBlank(message = "Último Nome não pode estar vazio")
    @Size(min = 2, max = 100, message = "Último Nome deve ter entre 2 e 100 caracteres")
    private String lastName;

    @NotNull(message = "E-mail é obrigatório")
    @NotBlank(message = "E-mail não pode estar vazio")
    @Email(message = "E-mail deve ter um formato válido")
    private String email;

    @NotNull(message = "Senha é obrigatória")
    @NotBlank(message = "Senha não pode estar vazia")
    @Size(min = 6, max = 50, message = "Senha deve ter entre 6 e 50 caracteres")
    private String password;
}
