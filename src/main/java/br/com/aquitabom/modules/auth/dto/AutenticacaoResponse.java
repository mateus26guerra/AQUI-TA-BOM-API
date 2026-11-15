package br.com.aquitabom.modules.auth.dto;

public record AutenticacaoResponse(String accessToken, String tokenType, long expiresIn, String refreshToken) {

    public static AutenticacaoResponse bearer(String accessToken, long expiresIn, String refreshToken) {
        return new AutenticacaoResponse(accessToken, "Bearer", expiresIn, refreshToken);
    }
}
