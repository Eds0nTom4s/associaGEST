package com.sistema.gestao.socios.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String documento; // Assuming CPF/CNPJ or similar identifier
    private String email;
    private String telefone;
    private String senha; // Consider hashing this in a real application
    private String statusPagamento; // e.g., "PAGO", "PENDENTE", "ATRASADO"

    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is generally preferred for performance
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "socio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos;

    @OneToMany(mappedBy = "socio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacao> notificacoes; // Added relationship for Notificacao
}
