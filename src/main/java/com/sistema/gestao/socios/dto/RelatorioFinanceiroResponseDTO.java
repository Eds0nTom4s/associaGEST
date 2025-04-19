package com.sistema.gestao.socios.dto;

import com.fasterxml.jackson.annotation.JsonFormat; // Import adicionado
import lombok.Data;
import java.util.Date;

@Data
public class RelatorioFinanceiroResponseDTO {
    private Long id;
    private String tipoRelatorio;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC") // Adicionado formato e timezone
    private Date periodoInicio;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC") // Adicionado formato e timezone
    private Date periodoFim;

    private String dadosRelatorio; // Keep as String, assuming pre-formatted data
}
