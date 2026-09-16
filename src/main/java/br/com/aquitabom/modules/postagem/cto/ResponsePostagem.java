package br.com.aquitabom.modules.postagem.cto;

import br.com.aquitabom.modules.postagem.Postagem;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ResponsePostagem(
        UUID id,
        String titulo,
        String descricao,
        String imagemUrl,
        String nomeUsuario,
        String nomeRestaurante,
        OffsetDateTime dataCriacao
) {
    public ResponsePostagem(Postagem p) {
        this(
            p.getId(),
            p.getTitulo(),
            p.getDescricao(),
            p.getImagemUrl(),
            p.getUsuario().getNome(),
            p.getRestaurante().getNome(),
            p.getDataCriacao()
        );
    }
}