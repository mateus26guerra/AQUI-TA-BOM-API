package br.com.aquitabom.core.security;

import java.util.List;

import br.com.aquitabom.modules.auth.AuthorityMapper;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final String CREDENCIAIS_INVALIDAS = "Credenciais inválidas";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityMapper authorityMapper;

    public CustomAuthenticationProvider(UsuarioRepository usuarioRepository,
                                        PasswordEncoder passwordEncoder,
                                        AuthorityMapper authorityMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityMapper = authorityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomAuthentication token = (CustomAuthentication) authentication;
        String email = String.valueOf(token.getPrincipal()).toLowerCase().trim();
        String senhaInformada = token.getCredentials() == null ? "" : token.getCredentials().toString();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(CREDENCIAIS_INVALIDAS));

        if (!usuario.isAtivo()) {
            throw new DisabledException("Usuário inativo");
        }
        if (!passwordEncoder.matches(senhaInformada, usuario.getSenha())) {
            throw new BadCredentialsException(CREDENCIAIS_INVALIDAS);
        }

        List<GrantedAuthority> authorities = authorityMapper.mapear(usuario);
        UsuarioAutenticado principal = new UsuarioAutenticado(
                usuario.getId(), usuario.getNome(), usuario.getEmail(), authorities);
        return CustomAuthentication.autenticado(principal, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthentication.class.isAssignableFrom(authentication);
    }
}
