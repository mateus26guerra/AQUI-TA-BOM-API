package br.com.aquitabom.modules.restaurante;

import br.com.aquitabom.core.Storage.StorageService;
import br.com.aquitabom.modules.LotacaoRestaurante.LotacaoRestaurante;
import br.com.aquitabom.modules.LotacaoRestaurante.LotacaoRestauranteRepository;
import br.com.aquitabom.modules.avaliacaolotacao.AvaliacaoLotacaoRepository;
import br.com.aquitabom.modules.avaliacaolotacao.AvaliacaoLotacaoService;
import br.com.aquitabom.modules.avaliacaolotacao.StatusLotacao;
import br.com.aquitabom.modules.avaliacaolotacao.dto.ResponseLotacao;
import br.com.aquitabom.modules.avaliacaorestaurante.AvaliacaoRestauranteRepository;
import br.com.aquitabom.modules.comentario.ComentarioRepository;
import br.com.aquitabom.modules.curtida.CurtidaRepository;
import br.com.aquitabom.modules.formapagamento.CategoriaPagamento;
import br.com.aquitabom.modules.formapagamento.FormaPagamento;
import br.com.aquitabom.modules.formapagamento.FormaPagamentoRepository;
import br.com.aquitabom.modules.formapagamento.dto.ResponseFormaPagamento;
import br.com.aquitabom.modules.postagem.Postagem;
import br.com.aquitabom.modules.postagem.PostagemRepository;
import br.com.aquitabom.modules.postagem.cto.ResponsePostagem;
import br.com.aquitabom.modules.restaurante.dto.Request.RequestAtualizarRestaurante;
import br.com.aquitabom.modules.restaurante.dto.Request.RequestRestaurante;
import br.com.aquitabom.modules.restaurante.dto.Response.ResponseRestaurante;
import br.com.aquitabom.modules.restaurante.dto.Response.ResponseRestauranteMapa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final StorageService storageService;
    private final PostagemRepository postagemRepository;
    private final LotacaoRestauranteRepository lotacaoRestauranteRepository;
    private final ComentarioRepository comentarioRepository;
    private final CurtidaRepository curtidaRepository;
    private final AvaliacaoLotacaoRepository avaliacaoLotacaoRepository;
    private final AvaliacaoRestauranteRepository avaliacaoRestauranteRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;

    public RestauranteService(
            RestauranteRepository restauranteRepository,
            StorageService storageService,
            PostagemRepository postagemRepository,
            LotacaoRestauranteRepository lotacaoRestauranteRepository,
            ComentarioRepository comentarioRepository,
            CurtidaRepository curtidaRepository,
            AvaliacaoLotacaoRepository avaliacaoLotacaoRepository,
            AvaliacaoRestauranteRepository avaliacaoRestauranteRepository,
            FormaPagamentoRepository formaPagamentoRepository
    ) {
        this.restauranteRepository = restauranteRepository;
        this.storageService = storageService;
        this.postagemRepository = postagemRepository;
        this.lotacaoRestauranteRepository = lotacaoRestauranteRepository;
        this.comentarioRepository = comentarioRepository;
        this.curtidaRepository = curtidaRepository;
        this.avaliacaoLotacaoRepository = avaliacaoLotacaoRepository;
        this.avaliacaoRestauranteRepository = avaliacaoRestauranteRepository;
        this.formaPagamentoRepository = formaPagamentoRepository;
    }
    @Transactional(readOnly = true)
    public List<ResponseRestauranteMapa> listarRestaurantesMapa(
            List<UUID> formasPagamentoIds,
            CategoriaPagamento categoria
    ) {

        var restaurantes = restauranteRepository.findAllComFormasPagamento()
                .stream()
                .filter(restaurante -> aceitaFormaPagamento(restaurante, formasPagamentoIds, categoria))
                .toList();

        var lotacoes = lotacaoRestauranteRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        lotacao -> lotacao.getRestaurante().getId(),
                        lotacao -> lotacao
                ));

        OffsetDateTime limite = OffsetDateTime.now().minusMinutes(30);

        return restaurantes.stream()
                .map(restaurante -> {

                    LotacaoRestaurante lotacao =
                            lotacoes.get(restaurante.getId());

                    StatusLotacao statusLotacao = null;

                    if (lotacao != null &&
                            lotacao.getAtualizadoEm().isAfter(limite)) {

                        statusLotacao = lotacao.getStatus();
                    }

                    return new ResponseRestauranteMapa(
                            restaurante.getId(),
                            restaurante.getNome(),
                            restaurante.getIniciais(),
                            restaurante.getLatitude(),
                            restaurante.getLongitude(),
                            restaurante.getEndereco(),
                            restaurante.getURLImagem(),
                            restaurante.getUrlCardapio(),
                            restaurante.getHorarioAbertura(),
                            restaurante.getHorarioFechamento(),
                            statusLotacao,
                            restaurante.getFormasPagamento().stream()
                                    .map(ResponseFormaPagamento::new)
                                    .sorted(Comparator.comparing(ResponseFormaPagamento::nome))
                                    .toList()
                    );
                })
                .toList();
    }
    @Transactional(readOnly = true)
    public Page<ResponsePostagem> listarPostagensDoRestaurante(
            UUID restauranteId,
            Pageable pageable
    ) {
        if (!restauranteRepository.existsById(restauranteId)) {
            throw new RuntimeException("Restaurante não encontrado");
        }
        return postagemRepository
                .findByRestauranteId(restauranteId, pageable)
                .map(this::montarResponsePostagem);
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
    public void salvarRestaurante(RequestRestaurante dto) {
        String urlImagem = null;

        // Se uma imagem foi enviada no DTO, faz o upload para o R2
        if (dto.imagem() != null && !dto.imagem().isEmpty()) {
            try {
                urlImagem = storageService.upload(dto.imagem());
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar imagem para o serviço de armazenamento", e);
            }
        }

        // Converte o DTO para a Entidade
        Restaurante restauranteNovo = new Restaurante();
        restauranteNovo.setNome(dto.nome());
        restauranteNovo.setIniciais(dto.iniciais());
        restauranteNovo.setLatitude(dto.latitude());
        restauranteNovo.setLongitude(dto.longitude());
        restauranteNovo.setEndereco(dto.endereco());
        restauranteNovo.setDescricao(dto.descricao());
        restauranteNovo.setTelefone(dto.telefone());
        restauranteNovo.setURLImagem(urlImagem);
        restauranteNovo.setUrlCardapio(dto.urlCardapio());
        restauranteNovo.setHorarioAbertura(dto.horarioAbertura());
        restauranteNovo.setHorarioFechamento(dto.horarioFechamento());
        restauranteNovo.setFormasPagamento(buscarFormasPagamento(dto.formasPagamentoIds()));

        // Salva a Entidade no banco
        restauranteRepository.save(restauranteNovo);
    }

    @Transactional(readOnly = true)
    public List<ResponseRestaurante> listarRestaurantes() {
        return listarRestaurantes(null, null);
    }

    @Transactional(readOnly = true)
    public List<ResponseRestaurante> listarRestaurantes(
            List<UUID> formasPagamentoIds,
            CategoriaPagamento categoria
    ) {
        return restauranteRepository.findAllComFormasPagamento()
                .stream()
                .filter(restaurante -> aceitaFormaPagamento(restaurante, formasPagamentoIds, categoria))
                .map(ResponseRestaurante::new)
                .toList();
    }

    // O restaurante passa no filtro se aceitar pelo menos uma das formas de pagamento informadas
    // (e, se a categoria for informada, essa forma também precisa ser da categoria)
    private boolean aceitaFormaPagamento(
            Restaurante restaurante,
            List<UUID> formasPagamentoIds,
            CategoriaPagamento categoria
    ) {
        boolean filtrarPorIds = formasPagamentoIds != null && !formasPagamentoIds.isEmpty();

        if (!filtrarPorIds && categoria == null) {
            return true;
        }

        return restaurante.getFormasPagamento().stream().anyMatch(forma ->
                (!filtrarPorIds || formasPagamentoIds.contains(forma.getId()))
                        && (categoria == null || forma.getCategoria() == categoria)
        );
    }

    private Set<FormaPagamento> buscarFormasPagamento(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        Set<UUID> idsUnicos = new HashSet<>(ids);
        List<FormaPagamento> formasPagamento = formaPagamentoRepository.findAllById(idsUnicos);

        if (formasPagamento.size() != idsUnicos.size()) {
            throw new IllegalArgumentException("Uma ou mais formas de pagamento informadas não existem");
        }

        return new HashSet<>(formasPagamento);
    }

    @Transactional
    public void deletarRestaurante(UUID id) {
        if (!restauranteRepository.existsById(id)) {
            throw new RuntimeException("Restaurante não encontrado com o ID: " + id);
        }

        // Remove primeiro os registros que dependem do restaurante (chaves estrangeiras)
        curtidaRepository.deleteByPostagemRestauranteId(id);
        comentarioRepository.deleteByPostagemRestauranteId(id);
        postagemRepository.deleteByRestauranteId(id);
        lotacaoRestauranteRepository.deleteByRestauranteId(id);
        avaliacaoLotacaoRepository.deleteByRestauranteId(id);
        avaliacaoRestauranteRepository.deleteByRestauranteId(id);

        restauranteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Restaurante buscarRestaurantePorId(UUID id) {
        return restauranteRepository.findById(id).orElseThrow(() -> new RuntimeException("Restaurante não encontrado com o ID: " + id));
    }

    @Transactional
    public void atualizarRestaurante(UUID id, RequestAtualizarRestaurante dto) {
        Restaurante restauranteAtualizado = buscarRestaurantePorId(id);

        if (dto.imagem() != null && !dto.imagem().isEmpty()) {
            try {
                String novaUrlImagem = storageService.upload(dto.imagem());
                restauranteAtualizado.setURLImagem(novaUrlImagem);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao atualizar imagem do restaurante", e);
            }
        }

        restauranteAtualizado.setNome(dto.nome());
        restauranteAtualizado.setEndereco(dto.endereco());
        restauranteAtualizado.setTelefone(dto.telefone());
        restauranteAtualizado.setLatitude(dto.latitude());
        restauranteAtualizado.setLongitude(dto.longitude());
        restauranteAtualizado.setDescricao(dto.descricao());
        restauranteAtualizado.setUrlCardapio(dto.urlCardapio());
        restauranteAtualizado.setHorarioAbertura(dto.horarioAbertura());
        restauranteAtualizado.setHorarioFechamento(dto.horarioFechamento());
        restauranteAtualizado.setFormasPagamento(buscarFormasPagamento(dto.formasPagamentoIds()));

        restauranteRepository.save(restauranteAtualizado);
    }
}