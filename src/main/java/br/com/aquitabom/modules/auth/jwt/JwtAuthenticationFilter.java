package br.com.aquitabom.modules.auth.jwt;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import br.com.aquitabom.modules.auth.AuthorityMapper;
import br.com.aquitabom.core.security.CustomAuthentication;
import br.com.aquitabom.core.security.UsuarioAutenticado;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lê o header {@code Authorization: Bearer <token>}, valida o JWT e, se válido,
 * popula o {@link SecurityContextHolder} com um {@link CustomAuthentication}
 * autenticado montado a partir das claims (sem consultar o banco).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final AuthorityMapper authorityMapper;

    public JwtAuthenticationFilter(JwtService jwtService, AuthorityMapper authorityMapper) {
        this.jwtService = jwtService;
        this.authorityMapper = authorityMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extrairToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                CustomAuthentication authentication = construirAutenticacao(jwtService.parseClaims(token));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException e) {
                logger.debug("Token JWT rejeitado: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private CustomAuthentication construirAutenticacao(Claims claims) {
        UUID id = UUID.fromString(claims.get("uid", String.class));
        String email = claims.getSubject();
        String nome = claims.get("nome", String.class);
        List<String> nomesAuthorities = claims.get("authorities", List.class);
        List<GrantedAuthority> authorities = authorityMapper.mapear(
                nomesAuthorities == null ? List.of() : nomesAuthorities);
        UsuarioAutenticado principal = new UsuarioAutenticado(id, nome, email, authorities);
        return CustomAuthentication.autenticado(principal, authorities);
    }

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (StringUtils.hasText(header) && header.startsWith(PREFIXO)) {
            String valor = header.substring(PREFIXO.length()).trim();
            return valor.isEmpty() ? null : valor;
        }
        return null;
    }
}
