package br.com.aquitabom.modules.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey chave;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.chave = Keys.hmacShaKeyFor(decodificarSegredo(properties.getSecret()));
    }

    public String gerarToken(UsuarioAutenticado usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(properties.getExpiration());
        return Jwts.builder()
                .subject(usuario.email())
                .claim("uid", usuario.id().toString())
                .claim("nome", usuario.nome())
                .claim("authorities", usuario.authorityNames())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave)
                .compact();
    }

    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiracaoSegundos() {
        return properties.getExpiration().toSeconds();
    }

    private static byte[] decodificarSegredo(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Propriedade 'security.jwt.secret' não configurada");
        }
        try {
            return Decoders.BASE64.decode(secret);
        } catch (DecodingException naoEhBase64) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
