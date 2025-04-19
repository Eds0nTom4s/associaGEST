package com.sistema.gestao.socios.dto;

import lombok.Data;

@Data
public class AdministradorResponseDTO {
    private Long id;
    private String nome;
    private String email;
    // Do not include password in the response DTO
}
