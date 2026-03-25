package com.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {

    @Schema(name = "mail", requiredMode = Schema.RequiredMode.REQUIRED, example = "juan@gmail.com", description = "correo electrónico")
    @NotBlank(message = "mail obligatorio")
    private String mail;

    @Schema(name = "password", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc123", description = "contraseña")
    @NotBlank(message = "contraseña obligatoria")
    private String password;

}
