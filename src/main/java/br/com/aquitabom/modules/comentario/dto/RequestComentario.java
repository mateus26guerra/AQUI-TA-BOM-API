package br.com.aquitabom.modules.comentario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestComentario(

        @NotBlank(message = "O comentário não pode estar vazio")
        @Size(max = 500, message = "O comentário deve ter no máximo 500 caracteres")
        String texto

) {}