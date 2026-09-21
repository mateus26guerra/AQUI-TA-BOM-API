package br.com.aquitabom.modules.avaliacaorestaurante.dto;

import br.com.aquitabom.modules.avaliacaorestaurante.AvaliacaoRestaurante;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ResponseAvaliacaoRestaurante(
        UUID id,
        UUID usuarioId,
        String nomeUsuario,
        UUID restauranteId,
        Integer nota,
        String comentario,
        OffsetDateTime dataCriacao
) {

    public ResponseAvaliacaoRestaurante(
            AvaliacaoRestaurante avaliacao
    ) {

        this(
                avaliacao.getId(),
                avaliacao.getUsuario().getId(),
                avaliacao.getUsuario().getNome(),
                avaliacao.getRestaurante().getId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataCriacao()
        );
    }
}