package com.sistema.gestao.socios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SocioRequestDTO {

    @NotBlank(message = "Nome não pode ser vazio")
    private String nome;

    @NotBlank(message = "Documento não pode ser vazio")
    // TODO: Add specific validation for document format (CPF/CNPJ) if needed
    private String documento;

    @NotBlank(message = "Email não pode ser vazio")
    @Email(message = "Formato de email inválido")
    private String email;

    private String telefone; // Optional

    @NotBlank(message = "Senha não pode ser vazia")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    // statusPagamento is likely managed internally, not set directly via request DTO
    // private String statusPagamento;

    @NotNull(message = "ID da Categoria não pode ser nulo")
    private Long categoriaId; // ID of the Categoria to associate with
}
