package br.com.aquitabom.modules.comentario;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.comentario.dto.RequestComentario;
import br.com.aquitabom.modules.comentario.dto.ResponseComentario;
import br.com.aquitabom.modules.postagem.Postagem;
import br.com.aquitabom.modules.postagem.PostagemRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PostagemRepository postagemRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            PostagemRepository postagemRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.comentarioRepository = comentarioRepository;
        this.postagemRepository = postagemRepository;
        this.usuarioRepository = usuarioRepository;
    }


    @Transactional
    public void deletarComentario(
            UUID comentarioId,
            UsuarioAutenticado principal
    ) {

        if (principal == null) {
            throw new IllegalStateException(
                    "Usuário autenticado não informado"
            );
        }

        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Comentário não encontrado"
                        )
                );

        if (!comentario.getUsuario().getId().equals(principal.id())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você não pode excluir o comentário de outro usuário"
            );
        }

        comentarioRepository.delete(comentario);
    }

    @Transactional
    public ResponseComentario criar(
            UUID postagemId,
            RequestComentario request,
            UsuarioAutenticado principal
    ) {

        if (principal == null) {
            throw new IllegalStateException(
                    "Usuário autenticado não informado"
            );
        }

        Postagem postagem = postagemRepository.findById(postagemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Postagem não encontrada"
                        )
                );

        Usuario usuario = usuarioRepository.findById(principal.id())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );

        Comentario comentario = new Comentario(
                request.texto(),
                usuario,
                postagem
        );

        comentarioRepository.save(comentario);

        return new ResponseComentario(comentario);
    }

    @Transactional(readOnly = true)
    public List<ResponseComentario> listarPorPostagem(UUID postagemId) {

        if (!postagemRepository.existsById(postagemId)) {
            throw new IllegalArgumentException(
                    "Postagem não encontrada"
            );
        }

        return comentarioRepository
                .findByPostagemIdOrderByDataCriacaoAsc(postagemId)
                .stream()
                .map(ResponseComentario::new)
                .toList();
    }
}