package com.sistema.gestao.socios.dto;

import com.fasterxml.jackson.annotation.JsonFormat; // Import adicionado
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;

@Data
public class RelatorioFinanceiroRequestDTO {

    @NotBlank(message = "Tipo do relatório não pode ser vazio")
    private String tipoRelatorio;

    @NotNull(message = "Data de início não pode ser nula")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC") // Adicionado formato e timezone
    private Date periodoInicio;

    @NotNull(message = "Data de fim não pode ser nula")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC") // Adicionado formato e timezone
    private Date periodoFim;

    // dadosRelatorio is generated, not provided in request
}
