package br.com.aquitabom.auth.security;

import java.io.Serial;
import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class CustomAuthentication extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Object principal;
    private Object credentials;

    private CustomAuthentication(Object principal,
                                 Object credentials,
                                 Collection<? extends GrantedAuthority> authorities,
                                 boolean autenticado) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(autenticado);
    }

    public static CustomAuthentication naoAutenticado(String email, String senha) {
        return new CustomAuthentication(email, senha, null, false);
    }

    public static CustomAuthentication autenticado(UsuarioAutenticado principal,
                                                   Collection<? extends GrantedAuthority> authorities) {
        return new CustomAuthentication(principal, null, authorities, true);
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    public String getEmail() {
        return principal instanceof UsuarioAutenticado usuario ? usuario.email() : String.valueOf(principal);
    }

    public UsuarioAutenticado getUsuario() {
        return principal instanceof UsuarioAutenticado usuario ? usuario : null;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Não é permitido elevar para autenticado — use CustomAuthentication.autenticado(...)");
        }
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
