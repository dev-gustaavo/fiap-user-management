package br.com.user_management.core.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
