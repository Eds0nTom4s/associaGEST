package com.sistema.gestao.socios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;

@Data
public class NotificacaoRequestDTO {

    @NotBlank(message = "Tipo da notificação não pode ser vazio")
    private String tipoNotificacao;

    // dataEnvio is usually set by the server
    // private Date dataEnvio;

    @NotBlank(message = "Mensagem não pode ser vazia")
    private String mensagem;

    @NotNull(message = "ID do Sócio não pode ser nulo")
    private Long socioId;
}
