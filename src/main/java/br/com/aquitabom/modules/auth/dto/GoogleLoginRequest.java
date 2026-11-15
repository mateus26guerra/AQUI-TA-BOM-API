package br.com.aquitabom.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(

        @NotBlank
        String idToken) {
}
