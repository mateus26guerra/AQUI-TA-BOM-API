package br.com.aquitabom.modules.comentario.dto;

import br.com.aquitabom.modules.comentario.Comentario;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ResponseComentario(
        UUID id,
        UUID usuarioId,
        String nomeUsuario,
        UUID postagemId,
        String texto,
        OffsetDateTime dataCriacao
) {

    public ResponseComentario(Comentario comentario) {
        this(
                comentario.getId(),
                comentario.getUsuario().getId(),
                comentario.getUsuario().getNome(),
                comentario.getPostagem().getId(),
                comentario.getTexto(),
                comentario.getDataCriacao()
        );
    }
}