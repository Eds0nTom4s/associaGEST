package com.sistema.gestao.socios.dto;

import com.fasterxml.jackson.annotation.JsonFormat; // Import adicionado
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PagamentoResponseDTO {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC") // Adicionado formato e timezone
    private Date dataPagamento;

    private BigDecimal valorPago;
    private String status;
    private Long socioId; // Only include Socio ID
    private Long categoriaId; // Only include Categoria ID

    // We avoid embedding full SocioResponseDTO or CategoriaResponseDTO
    // to prevent overly complex responses and potential circular references.
    // Clients can fetch full details using the IDs if needed.
}
