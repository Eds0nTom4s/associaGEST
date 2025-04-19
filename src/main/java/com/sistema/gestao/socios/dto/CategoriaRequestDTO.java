package com.sistema.gestao.socios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoriaRequestDTO {

    @NotBlank(message = "Nome da categoria não pode ser vazio")
    private String nome;

    private String beneficios; // Optional

    @NotNull(message = "Valor da mensalidade não pode ser nulo")
    @PositiveOrZero(message = "Valor da mensalidade deve ser positivo ou zero")
    private BigDecimal valorMensalidade;
}
