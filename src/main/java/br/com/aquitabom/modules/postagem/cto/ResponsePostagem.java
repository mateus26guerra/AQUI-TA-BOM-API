package br.com.aquitabom.modules.postagem.cto;

import br.com.aquitabom.modules.postagem.Postagem;
import br.com.aquitabom.modules.postagem.StatusPostagem;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponsePostagem(
        UUID id,
        String titulo,
        String descricao,
        String imagemUrl,
        Long likes,
        Integer nota,
        StatusPostagem status,
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
            p.getLikes(),
            p.getNota(),
            p.getStatus(),
            p.getUsuario().getNome(),
            p.getRestaurante().getNome(),
            p.getDataCriacao()
        );
    }
}
