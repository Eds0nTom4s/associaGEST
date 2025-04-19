package com.sistema.gestao.socios.dto;

import com.fasterxml.jackson.annotation.JsonFormat; // Import adicionado
import lombok.Data;
import java.util.Date;

@Data
public class NotificacaoResponseDTO {
    private Long id;
    private String tipoNotificacao;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC") // Adicionado formato e timezone
    private Date dataEnvio;

    private String mensagem;
    private Long socioId; // Only include Socio ID
}
