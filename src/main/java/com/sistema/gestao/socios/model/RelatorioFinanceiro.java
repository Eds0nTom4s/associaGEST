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
public class RelatorioFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoRelatorio; // e.g., "MENSAL", "ANUAL", "POR_CATEGORIA"

    @Temporal(TemporalType.DATE)
    private Date periodoInicio;

    @Temporal(TemporalType.DATE)
    private Date periodoFim;

    @Lob // Use @Lob for potentially large report data (e.g., JSON, CSV)
    private String dadosRelatorio;
}
