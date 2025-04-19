package com.sistema.gestao.socios.dto;

import lombok.Data;

@Data
public class SocioResponseDTO {
    private Long id;
    private String nome;
    private String documento;
    private String email;
    private String telefone;
    private String statusPagamento;
    private CategoriaResponseDTO categoria; // Embed Categoria details (using its DTO)

    // Note: We are not including the password, list of Pagamentos, or Notificacoes
    // to avoid exposing sensitive data and prevent large/circular responses.
}
