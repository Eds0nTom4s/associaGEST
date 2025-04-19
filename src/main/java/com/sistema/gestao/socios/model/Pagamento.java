package com.sistema.gestao.socios.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP) // Use TIMESTAMP for date and time
    private Date dataPagamento;

    private BigDecimal valorPago;
    private String status; // e.g., "CONFIRMADO", "PENDENTE", "REJEITADO"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id")
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id") // Added relationship to Categoria
    private Categoria categoria;
}
