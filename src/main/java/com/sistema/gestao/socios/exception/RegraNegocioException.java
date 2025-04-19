package com.sistema.gestao.socios.exception;

// This exception can be used for various business rule violations
// like duplicate entries, invalid operations, etc.
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
