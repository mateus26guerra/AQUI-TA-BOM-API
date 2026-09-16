package br.com.aquitabom.auth.dto;

import java.util.List;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        List<String> roles,
        List<String> authorities) {
}
