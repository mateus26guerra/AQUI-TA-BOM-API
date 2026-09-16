package br.com.aquitabom.auth.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

public record UsuarioAutenticado(
        UUID id,
        String nome,
        String email,
        Collection<? extends GrantedAuthority> authorities) {

    public List<String> authorityNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
    }
}
