package br.com.aquitabom.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarNomeRequest(

        @NotBlank
        @Size(min = 3, max = 120)
        String nome) {
}
