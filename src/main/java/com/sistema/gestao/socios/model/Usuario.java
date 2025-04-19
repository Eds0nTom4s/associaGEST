package com.sistema.gestao.socios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder; // Import Builder
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@Builder // Add Builder annotation
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha; // Store hashed password

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // UserDetails methods implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Prefix ROLE_ is standard in Spring Security
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email; // Use email as the username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or implement logic based on your requirements
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or implement logic based on your requirements
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or implement logic based on your requirements
    }

    @Override
    public boolean isEnabled() {
        return true; // Or implement logic based on your requirements
    }
}
