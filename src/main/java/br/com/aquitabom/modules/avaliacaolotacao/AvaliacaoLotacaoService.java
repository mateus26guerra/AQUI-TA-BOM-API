package br.com.aquitabom.modules.avaliacaolotacao;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.LotacaoRestaurante.LotacaoRestaurante;
import br.com.aquitabom.modules.LotacaoRestaurante.LotacaoRestauranteRepository;
import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.restaurante.RestauranteRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import br.com.aquitabom.modules.avaliacaolotacao.dto.ResponseLotacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AvaliacaoLotacaoService {

    private final AvaliacaoLotacaoRepository avaliacaoRepository;
    private final RestauranteRepository restauranteRepository;
    private final UsuarioRepository usuarioRepository;
    private final LotacaoRestauranteRepository lotacaoRepository;

    public AvaliacaoLotacaoService(
            AvaliacaoLotacaoRepository avaliacaoRepository,
            RestauranteRepository restauranteRepository,
            UsuarioRepository usuarioRepository,
            LotacaoRestauranteRepository lotacaoRepository
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.restauranteRepository = restauranteRepository;
        this.usuarioRepository = usuarioRepository;
        this.lotacaoRepository = lotacaoRepository;
    }

    // =========================================================
    // USUÁRIO AVALIA O RESTAURANTE
    // =========================================================

    @Transactional
    public void avaliar(
            UUID restauranteId,
            StatusLotacao status,
            UsuarioAutenticado principal
    ) {

        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Restaurante não encontrado")
                );

        Usuario usuario = usuarioRepository.findById(principal.id())
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado")
                );

        avaliacaoRepository
                .findByUsuarioIdAndRestauranteId(
                        principal.id(),
                        restauranteId
                )
                .map(avaliacao -> {
                    avaliacao.setStatus(status);
                    avaliacao.setDataCriacao(OffsetDateTime.now());
                    return avaliacaoRepository.save(avaliacao);
                })
                .orElseGet(() -> {

                    AvaliacaoLotacao avaliacao =
                            new AvaliacaoLotacao(
                                    restaurante,
                                    usuario,
                                    status
                            );

                    return avaliacaoRepository.save(avaliacao);
                });

        atualizarLotacaoRestaurante(restaurante);
    }
    private void atualizarLotacaoRestaurante(Restaurante restaurante) {

        OffsetDateTime limite =
                OffsetDateTime.now().minusMinutes(30);

        List<AvaliacaoLotacao> avaliacoes =
                avaliacaoRepository
                        .findByRestauranteIdAndDataCriacaoAfter(
                                restaurante.getId(),
                                limite
                        );

        long deBoa = avaliacoes.stream()
                .filter(a -> a.getStatus() == StatusLotacao.DE_BOA)
                .count();

        long embacado = avaliacoes.stream()
                .filter(a -> a.getStatus() == StatusLotacao.EMBACADO)
                .count();

        long cheioQueSo = avaliacoes.stream()
                .filter(a -> a.getStatus() == StatusLotacao.CHEIO_QUE_SO)
                .count();

        if (avaliacoes.isEmpty()) {
            return;
        }
        StatusLotacao resultado = StatusLotacao.DE_BOA;

        if (embacado > deBoa && embacado >= cheioQueSo) {
            resultado = StatusLotacao.EMBACADO;
        }

        if (cheioQueSo > deBoa && cheioQueSo > embacado) {
            resultado = StatusLotacao.CHEIO_QUE_SO;
        }

        final StatusLotacao statusFinal = resultado;

        LotacaoRestaurante lotacao =
                lotacaoRepository
                        .findByRestauranteId(restaurante.getId())
                        .orElseGet(() ->
                                new LotacaoRestaurante(
                                        restaurante,
                                        statusFinal
                                )
                        );

        lotacao.atualizar(statusFinal);

        lotacaoRepository.save(lotacao);
    }

    // =========================================================
    // BUSCA AS AVALIAÇÕES DOS ÚLTIMOS 30 MINUTOS
    // =========================================================

    @Transactional(readOnly = true)
    public List<AvaliacaoLotacao> buscarAvaliacoesRecentes(
            UUID restauranteId
    ) {

        OffsetDateTime limite =
                OffsetDateTime.now().minusMinutes(30);

        return avaliacaoRepository
                .findByRestauranteIdAndDataCriacaoAfter(
                        restauranteId,
                        limite
                );
    }


    // =========================================================
    // CALCULA A SITUAÇÃO ATUAL DO RESTAURANTE
    // =========================================================

    @Transactional(readOnly = true)
    public ResponseLotacao consultarLotacao(
            UUID restauranteId
    ) {

        List<AvaliacaoLotacao> avaliacoes =
                buscarAvaliacoesRecentes(restauranteId);

        long deBoa = avaliacoes.stream()
                .filter(a ->
                        a.getStatus() == StatusLotacao.DE_BOA)
                .count();

        long embacado = avaliacoes.stream()
                .filter(a ->
                        a.getStatus() == StatusLotacao.EMBACADO)
                .count();

        long cheioQueSo = avaliacoes.stream()
                .filter(a ->
                        a.getStatus() == StatusLotacao.CHEIO_QUE_SO)
                .count();


        StatusLotacao resultado = null;


        if (!avaliacoes.isEmpty()) {

            resultado = StatusLotacao.DE_BOA;

            if (embacado > deBoa &&
                    embacado >= cheioQueSo) {

                resultado = StatusLotacao.EMBACADO;
            }

            if (cheioQueSo > deBoa &&
                    cheioQueSo > embacado) {

                resultado = StatusLotacao.CHEIO_QUE_SO;
            }
        }


        return new ResponseLotacao(
                restauranteId,
                resultado,
                deBoa,
                embacado,
                cheioQueSo,
                avaliacoes.size()
        );
    }
}