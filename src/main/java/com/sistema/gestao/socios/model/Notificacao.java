package com.sistema.gestao.socios.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoNotificacao; // e.g., "PAGAMENTO_PENDENTE", "EVENTO", "AVISO"

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataEnvio;

    @Lob // Use @Lob for potentially large text fields
    private String mensagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id")
    private Socio socio;
}
