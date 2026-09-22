package br.com.aquitabom.modules.postagem;

import br.com.aquitabom.core.Storage.StorageService;
import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.comentario.ComentarioRepository;
import br.com.aquitabom.modules.curtida.Curtida;
import br.com.aquitabom.modules.curtida.CurtidaRepository;
import br.com.aquitabom.modules.postagem.cto.ResponseCurtida;
import br.com.aquitabom.modules.postagem.cto.ResponsePostagem;
import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.restaurante.RestauranteRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class PostagemService {

    private final PostagemRepository postagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final CurtidaRepository curtidaRepository;
    private final ComentarioRepository comentarioRepository;
    private final StorageService storageService;

    public PostagemService(
            PostagemRepository postagemRepository,
            UsuarioRepository usuarioRepository,
            RestauranteRepository restauranteRepository,
            CurtidaRepository curtidaRepository,
            ComentarioRepository comentarioRepository,
            StorageService storageService
    ) {
        this.postagemRepository = postagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;
        this.curtidaRepository = curtidaRepository;
        this.comentarioRepository = comentarioRepository;
        this.storageService = storageService;
    }


    private ResponsePostagem montarResponsePostagem(Postagem postagem) {

        Long quantidadeComentarios =
                comentarioRepository.countByPostagemId(postagem.getId());

        List<String> nomesUsuariosCurtiram =
                curtidaRepository
                        .findTop2ByPostagemIdOrderByDataCriacaoDesc(postagem.getId())
                        .stream()
                        .map(curtida -> curtida.getUsuario().getNome())
                        .toList();

        return new ResponsePostagem(
                postagem,
                quantidadeComentarios,
                nomesUsuariosCurtiram
        );
    }

    @Transactional
    public void deletarPostagem(
            UUID postagemId,
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

        if (!postagem.getUsuario().getId().equals(principal.id())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você não pode excluir uma postagem de outro usuário"
            );
        }

        // Remove comentários da postagem
        comentarioRepository.deleteByPostagemId(postagemId);

        // Remove curtidas da postagem
        curtidaRepository.deleteByPostagemId(postagemId);

        // Remove a postagem
        postagemRepository.delete(postagem);
    }

    @Transactional(readOnly = true)
    public List<ResponsePostagem> listarPostagensDoUsuario(
            UUID usuarioId
    ) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException(
                    "Usuário não encontrado"
            );
        }

        return postagemRepository
                .findByUsuarioIdOrderByDataCriacaoDesc(usuarioId)
                .stream()
                .map(this::montarResponsePostagem)
                .toList();
    }

    @Transactional
    public ResponsePostagem criarPostagem(
            String titulo,
            String descricao,
            UUID restauranteId,
            MultipartFile imagem,
            UsuarioAutenticado principal,
            Integer nota,
            StatusPostagem status
    ) {

        if (principal == null) {
            throw new IllegalStateException(
                    "Usuário autenticado não informado"
            );
        }

        if (nota == null || nota < 1 || nota > 5) {
            throw new IllegalArgumentException(
                    "A nota deve estar entre 1 e 5"
            );
        }

        Usuario usuario = usuarioRepository.findById(principal.id())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );

        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Restaurante não encontrado"
                        )
                );

        String urlImagem;

        try {
            urlImagem = storageService.upload(imagem);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao enviar imagem para o Cloudflare R2",
                    e
            );
        }

        Postagem postagem = new Postagem(
                titulo,
                descricao,
                urlImagem,
                usuario,
                restaurante,
                nota,
                status
        );

        postagemRepository.save(postagem);

        return montarResponsePostagem(postagem);
    }

    @Transactional(readOnly = true)
    public List<ResponsePostagem> listarTodas() {

        return postagemRepository
                .findAllByOrderByDataCriacaoDesc()
                .stream()
                .map(this::montarResponsePostagem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResponsePostagem> listarMinhasPostagens(
            UsuarioAutenticado principal
    ) {

        if (principal == null) {
            throw new IllegalStateException(
                    "Usuário autenticado não informado"
            );
        }

        return postagemRepository
                .findAllByUsuarioIdOrderByDataCriacaoDesc(principal.id())
                .stream()
                .map(this::montarResponsePostagem)
                .toList();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ResponsePostagem> listarPostagensDoRestaurante(
            UUID restauranteId,
            org.springframework.data.domain.Pageable pageable
    ) {

        if (!restauranteRepository.existsById(restauranteId)) {
            throw new IllegalArgumentException(
                    "Restaurante não encontrado"
            );
        }

        return postagemRepository
                .findByRestauranteId(restauranteId, pageable)
                .map(this::montarResponsePostagem);
    }

    @Transactional
    public ResponseCurtida curtir(
            UUID postagemId,
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

        if (!curtidaRepository.existsByPostagemIdAndUsuarioId(
                postagemId,
                principal.id()
        )) {

            Usuario usuario = usuarioRepository.findById(principal.id())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Usuário não encontrado"
                            )
                    );

            curtidaRepository.save(
                    new Curtida(postagem, usuario)
            );

            postagemRepository.incrementarLikes(postagemId);
        }

        return montarRespostaCurtida(postagemId, true);
    }

    @Transactional
    public ResponseCurtida descurtir(
            UUID postagemId,
            UsuarioAutenticado principal
    ) {

        if (principal == null) {
            throw new IllegalStateException(
                    "Usuário autenticado não informado"
            );
        }

        if (!postagemRepository.existsById(postagemId)) {
            throw new IllegalArgumentException(
                    "Postagem não encontrada"
            );
        }

        if (curtidaRepository.deleteByPostagemIdAndUsuarioId(
                postagemId,
                principal.id()
        ) > 0) {

            postagemRepository.decrementarLikes(postagemId);
        }

        return montarRespostaCurtida(postagemId, false);
    }

    private ResponseCurtida montarRespostaCurtida(
            UUID postagemId,
            boolean curtido
    ) {

        Long likes = postagemRepository
                .findById(postagemId)
                .map(Postagem::getLikes)
                .orElse(0L);

        return new ResponseCurtida(
                postagemId,
                likes,
                curtido
        );
    }
}