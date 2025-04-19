package com.sistema.gestao.socios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdministradorRequestDTO {

    @NotBlank(message = "Nome não pode ser vazio")
    private String nome;

    @NotBlank(message = "Email não pode ser vazio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "Senha não pode ser vazia")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres") // Example: Stronger password for admin
    private String senha;
}
