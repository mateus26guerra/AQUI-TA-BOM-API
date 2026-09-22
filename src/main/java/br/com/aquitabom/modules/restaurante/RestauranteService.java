package br.com.aquitabom.modules.restaurante;

import br.com.aquitabom.core.Storage.StorageService;
import br.com.aquitabom.modules.LotacaoRestaurante.LotacaoRestaurante;
import br.com.aquitabom.modules.LotacaoRestaurante.LotacaoRestauranteRepository;
import br.com.aquitabom.modules.avaliacaolotacao.AvaliacaoLotacaoService;
import br.com.aquitabom.modules.avaliacaolotacao.StatusLotacao;
import br.com.aquitabom.modules.avaliacaolotacao.dto.ResponseLotacao;
import br.com.aquitabom.modules.comentario.ComentarioRepository;
import br.com.aquitabom.modules.curtida.CurtidaRepository;
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
import java.util.List;
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

    public RestauranteService(
            RestauranteRepository restauranteRepository,
            StorageService storageService,
            PostagemRepository postagemRepository,
            LotacaoRestauranteRepository lotacaoRestauranteRepository,
            ComentarioRepository comentarioRepository,
            CurtidaRepository curtidaRepository
    ) {
        this.restauranteRepository = restauranteRepository;
        this.storageService = storageService;
        this.postagemRepository = postagemRepository;
        this.lotacaoRestauranteRepository = lotacaoRestauranteRepository;
        this.comentarioRepository = comentarioRepository;
        this.curtidaRepository = curtidaRepository;
    }
    @Transactional(readOnly = true)
    public List<ResponseRestauranteMapa> listarRestaurantesMapa() {

        var restaurantes = restauranteRepository.findAll();

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
                            statusLotacao
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

        // Salva a Entidade no banco
        restauranteRepository.save(restauranteNovo);
    }

    @Transactional(readOnly = true)
    public List<ResponseRestaurante> listarRestaurantes() {
        return restauranteRepository.findAll().stream().map(ResponseRestaurante::new).toList();
    }

    @Transactional
    public void deletarRestaurante(UUID id) {
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

        restauranteRepository.save(restauranteAtualizado);
    }
}