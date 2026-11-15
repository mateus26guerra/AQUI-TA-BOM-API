package br.com.aquitabom.modules.postagem;

import br.com.aquitabom.core.Storage.StorageService;
import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.comentario.ComentarioRepository;
import br.com.aquitabom.modules.curtida.Curtida;
import br.com.aquitabom.modules.curtida.CurtidaRepository;
import br.com.aquitabom.modules.postagem.cto.ResponseCurtida;
import br.com.aquitabom.modules.postagem.cto.ResponsePostagem;
import br.com.aquitabom.modules.recomendacao.RecomendacaoClient;
import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.restaurante.RestauranteRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostagemService {

    private final PostagemRepository postagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final CurtidaRepository curtidaRepository;
    private final ComentarioRepository comentarioRepository;
    private final StorageService storageService;
    private final RecomendacaoClient recomendacaoClient;

    // Limites para a chamada ao serviço de NLP não ficar pesada
    private static final int MAX_CURTIDAS_NO_PERFIL = 20;
    private static final int MAX_CANDIDATAS = 200;
    // Quantas das primeiras recomendadas ganham o selo "Recomendado pra você" no app
    private static final int QTD_COM_SELO = 3;

    public PostagemService(
            PostagemRepository postagemRepository,
            UsuarioRepository usuarioRepository,
            RestauranteRepository restauranteRepository,
            CurtidaRepository curtidaRepository,
            ComentarioRepository comentarioRepository,
            StorageService storageService,
            RecomendacaoClient recomendacaoClient
    ) {
        this.postagemRepository = postagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;
        this.curtidaRepository = curtidaRepository;
        this.comentarioRepository = comentarioRepository;
        this.storageService = storageService;
        this.recomendacaoClient = recomendacaoClient;
    }


    private ResponsePostagem montarResponsePostagem(Postagem postagem) {
        return montarResponsePostagem(postagem, null);
    }

    private ResponsePostagem montarResponsePostagem(Postagem postagem, Boolean recomendada) {

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
                nomesUsuariosCurtiram,
                recomendada
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

    /**
     * Feed personalizado: as postagens que o usuário ainda não curtiu (e que não são dele) vêm primeiro,
     * ordenadas pela semelhança com o que ele já curtiu. Depois vem o resto do feed, do mais novo ao mais antigo.
     * Se ele ainda não curtiu nada, ou se o serviço de recomendação falhar, devolve o feed na ordem normal.
     */
    @Transactional(readOnly = true)
    public List<ResponsePostagem> listarRecomendadas(UsuarioAutenticado principal) {
        List<Postagem> feed = postagemRepository.findAllByOrderByDataCriacaoDesc();

        List<Postagem> curtidas = curtidaRepository.findByUsuarioIdOrderByDataCriacaoDesc(principal.id())
                .stream()
                .map(Curtida::getPostagem)
                .toList();
        if (curtidas.isEmpty()) {
            return feed.stream().map(this::montarResponsePostagem).toList();
        }

        Set<UUID> idsCurtidos = curtidas.stream().map(Postagem::getId).collect(Collectors.toSet());
        List<Postagem> candidatas = feed.stream()
                .filter(p -> !idsCurtidos.contains(p.getId()))
                .filter(p -> !p.getUsuario().getId().equals(principal.id()))
                .limit(MAX_CANDIDATAS)
                .toList();
        if (candidatas.isEmpty()) {
            return feed.stream().map(this::montarResponsePostagem).toList();
        }

        List<String> perfil = curtidas.stream()
                .limit(MAX_CURTIDAS_NO_PERFIL)
                .map(PostagemService::textoParaRecomendacao)
                .toList();
        List<RecomendacaoClient.Candidata> pedido = candidatas.stream()
                .map(p -> new RecomendacaoClient.Candidata(p.getId().toString(), textoParaRecomendacao(p)))
                .toList();

        List<String> ordem = recomendacaoClient.ordenar(perfil, pedido).orElse(null);
        if (ordem == null) {
            return feed.stream().map(this::montarResponsePostagem).toList();
        }

        // Reordena seguindo a resposta do serviço (a ordem do banco não é confiável para isso)
        Map<String, Postagem> porId = candidatas.stream()
                .collect(Collectors.toMap(p -> p.getId().toString(), Function.identity()));
        List<ResponsePostagem> resultado = new ArrayList<>();
        Set<UUID> jaIncluidas = new HashSet<>();
        for (String id : ordem) {
            Postagem p = porId.get(id);
            if (p != null && jaIncluidas.add(p.getId())) {
                boolean selo = resultado.size() < QTD_COM_SELO;
                resultado.add(montarResponsePostagem(p, selo));
            }
        }
        for (Postagem p : feed) {
            if (jaIncluidas.add(p.getId())) {
                resultado.add(montarResponsePostagem(p));
            }
        }
        return resultado;
    }

    // Formato de texto sugerido pelo serviço: título, descrição e restaurante
    private static String textoParaRecomendacao(Postagem p) {
        StringBuilder texto = new StringBuilder("Titulo: ").append(p.getTitulo()).append('.');
        if (p.getDescricao() != null && !p.getDescricao().isBlank()) {
            texto.append(" Descricao: ").append(p.getDescricao().trim()).append('.');
        }
        texto.append(" Restaurante: ").append(p.getRestaurante().getNome()).append('.');
        return texto.toString();
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