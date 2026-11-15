package br.com.aquitabom.modules.auth.refresh;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

import br.com.aquitabom.modules.auth.jwt.JwtProperties;
import br.com.aquitabom.modules.usuario.Usuario;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private static final String TOKEN_INVALIDO = "Sessão expirada, faça login novamente";

    private final SecureRandom random = new SecureRandom();
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties properties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties properties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.properties = properties;
    }

    @Transactional
    public String criar(Usuario usuario) {
        OffsetDateTime agora = OffsetDateTime.now();
        refreshTokenRepository.deleteExpiradosDoUsuario(usuario.getId(), agora);

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        refreshTokenRepository.save(new RefreshToken(
                hash(token), usuario, agora.plus(properties.getRefreshExpiration())));
        return token;
    }

    /**
     * Valida e invalida o refresh token (rotação: cada token só pode ser usado uma vez).
     * Retorna o dono do token para que um novo par de tokens seja emitido.
     */
    @Transactional(noRollbackFor = {BadCredentialsException.class, DisabledException.class})
    public Usuario consumir(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new BadCredentialsException(TOKEN_INVALIDO));

        refreshTokenRepository.delete(refreshToken);

        if (refreshToken.isExpirado()) {
            throw new BadCredentialsException(TOKEN_INVALIDO);
        }

        Usuario usuario = refreshToken.getUsuario();
        if (!usuario.isAtivo()) {
            throw new DisabledException("Usuário inativo");
        }
        return usuario;
    }

    @Transactional
    public void revogar(String token) {
        refreshTokenRepository.deleteByTokenHash(hash(token));
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
