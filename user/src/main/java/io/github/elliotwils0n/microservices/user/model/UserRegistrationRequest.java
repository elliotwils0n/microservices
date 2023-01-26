package io.github.elliotwils0n.microservices.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserRegistrationRequest {

    private String username;
    private String email;
    private String password;

}
