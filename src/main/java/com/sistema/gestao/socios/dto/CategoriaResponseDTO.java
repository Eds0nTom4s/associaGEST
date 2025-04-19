package com.sistema.gestao.socios.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoriaResponseDTO {
    private Long id;
    private String nome;
    private String beneficios;
    private BigDecimal valorMensalidade;
    // Note: We are not including the list of Socios or Pagamentos here
    // to avoid circular dependencies and large responses.
    // If needed, specific endpoints can be created to fetch related entities.
}
