package com.sistema.gestao.socios.controller;

import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.RegisterRequestDTO;
import com.sistema.gestao.socios.service.AuthenticationService;
import jakarta.validation.Valid; // Importar @Valid
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request // Adicionar @Valid
    ) {
        // Delegate registration logic to the service
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @RequestBody LoginRequestDTO request
    ) {
        // Delegate login logic to the service
        return ResponseEntity.ok(authenticationService.login(request));
    }
}
