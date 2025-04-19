package com.sistema.gestao.socios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PagamentoRequestDTO {

    // dataPagamento might be set automatically by the server upon registration
    // private Date dataPagamento;

    @NotNull(message = "Valor pago não pode ser nulo")
    @Positive(message = "Valor pago deve ser positivo")
    private BigDecimal valorPago;

    @NotBlank(message = "Status não pode ser vazio")
    private String status; // e.g., "CONFIRMADO", "PENDENTE" - Initial status might be set by logic

    @NotNull(message = "ID do Sócio não pode ser nulo")
    private Long socioId;

    @NotNull(message = "ID da Categoria não pode ser nulo")
    private Long categoriaId;
}
