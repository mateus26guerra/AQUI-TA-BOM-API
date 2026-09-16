package br.com.aquitabom.modules.auth.dto;

public record AutenticacaoResponse(String accessToken, String tokenType, long expiresIn) {

    public static AutenticacaoResponse bearer(String accessToken, long expiresIn) {
        return new AutenticacaoResponse(accessToken, "Bearer", expiresIn);
    }
}
