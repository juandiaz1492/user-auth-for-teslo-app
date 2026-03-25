package com.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "User", description = "Datos de entrada para crear un usuario")
public class UserDto {
    
    @Schema(
            name = "username",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Juan",
            description = "Nombre del usuario"
    )
    @NotBlank(message = "nombre obligatorio")
    String username; 

    @Schema(
            name = "mail",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "juan@gmail.com",
            description = "correo electrónico"
    )
    @NotBlank(message = "mail obligatorio")
    String mail; 

    @Schema(
            name = "password",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Abc123",
            description = "contraseña"
    )
    @NotBlank(message = "contraseña obligatoria")
    String password; 

}
