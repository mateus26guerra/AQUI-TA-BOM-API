package br.com.aquitabom.modules.postagem.cto;

import br.com.aquitabom.modules.postagem.Postagem;
import br.com.aquitabom.modules.postagem.StatusPostagem;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponsePostagem(
        UUID id,
        UUID usuarioId,
        String titulo,
        String descricao,
        String imagemUrl,
        Long likes,
        Integer nota,
        StatusPostagem status,
        String nomeUsuario,
        String nomeRestaurante,
        OffsetDateTime dataCriacao,
        Long quantidadeComentarios,
        List<String> nomesUsuariosCurtiram
) {

    public ResponsePostagem(
            Postagem p,
            Long quantidadeComentarios,
            List<String> nomesUsuariosCurtiram
    ) {
        this(
                p.getId(),
                p.getUsuario().getId(),
                p.getTitulo(),
                p.getDescricao(),
                p.getImagemUrl(),
                p.getLikes(),
                p.getNota(),
                p.getStatus(),
                p.getUsuario().getNome(),
                p.getRestaurante().getNome(),
                p.getDataCriacao(),
                quantidadeComentarios,
                nomesUsuariosCurtiram
        );
    }
}