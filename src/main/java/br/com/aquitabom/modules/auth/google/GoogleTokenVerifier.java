package br.com.aquitabom.modules.auth.google;

import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {

    private static final String TOKEN_INVALIDO = "Token do Google inválido";

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${security.google.client-id}") String clientId) {
        // Confere assinatura (chaves públicas do Google), emissor, validade e se o token foi gerado para o nosso app
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();
    }

    public UsuarioGoogle verificar(String idToken) {
        GoogleIdToken token;
        try {
            token = verifier.verify(idToken);
        } catch (Exception e) {
            throw new BadCredentialsException(TOKEN_INVALIDO);
        }
        if (token == null) {
            throw new BadCredentialsException(TOKEN_INVALIDO);
        }

        GoogleIdToken.Payload payload = token.getPayload();
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new BadCredentialsException("O e-mail da conta Google não está verificado");
        }

        String nome = (String) payload.get("name");
        return new UsuarioGoogle(payload.getEmail(), nome);
    }

    public record UsuarioGoogle(String email, String nome) {
    }
}
