package br.com.aquitabom.modules.avaliacaorestaurante;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.avaliacaorestaurante.dto.RequestAvaliacaoRestaurante;
import br.com.aquitabom.modules.avaliacaorestaurante.dto.ResponseAvaliacaoRestaurante;
import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.restaurante.RestauranteRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AvaliacaoRestauranteService {

    private final AvaliacaoRestauranteRepository avaliacaoRepository;
    private final RestauranteRepository restauranteRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoRestauranteService(
            AvaliacaoRestauranteRepository avaliacaoRepository,
            RestauranteRepository restauranteRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.restauranteRepository = restauranteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void deletarAvaliacao(
            UUID avaliacaoId,
            UsuarioAutenticado principal
    ) {

        if (principal == null) {
            throw new IllegalStateException(
                    "Usuário autenticado não informado"
            );
        }

        AvaliacaoRestaurante avaliacao =
                avaliacaoRepository.findById(avaliacaoId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Avaliação não encontrada"
                                )
                        );

        if (!avaliacao.getUsuario().getId().equals(principal.id())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você não pode excluir a avaliação de outro usuário"
            );
        }

        avaliacaoRepository.delete(avaliacao);
    }

    @Transactional
    public ResponseAvaliacaoRestaurante avaliar(
            UUID restauranteId,
            RequestAvaliacaoRestaurante request,
            UsuarioAutenticado principal
    ) {

        if (principal == null) {
            throw new IllegalStateException(
                    "Usuário autenticado não informado"
            );
        }

        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Restaurante não encontrado"
                        )
                );

        Usuario usuario = usuarioRepository.findById(principal.id())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );

        AvaliacaoRestaurante avaliacao =
                avaliacaoRepository
                        .findByUsuarioIdAndRestauranteId(
                                principal.id(),
                                restauranteId
                        )
                        .orElse(null);

        if (avaliacao == null) {

            avaliacao = new AvaliacaoRestaurante(
                    request.nota(),
                    request.comentario(),
                    usuario,
                    restaurante
            );

        } else {

            avaliacao.atualizar(
                    request.nota(),
                    request.comentario()
            );
        }

        avaliacaoRepository.save(avaliacao);

        return new ResponseAvaliacaoRestaurante(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<ResponseAvaliacaoRestaurante> listarPorRestaurante(
            UUID restauranteId
    ) {

        if (!restauranteRepository.existsById(restauranteId)) {
            throw new IllegalArgumentException(
                    "Restaurante não encontrado"
            );
        }

        return avaliacaoRepository
                .findByRestauranteIdOrderByDataCriacaoDesc(restauranteId)
                .stream()
                .map(ResponseAvaliacaoRestaurante::new)
                .toList();
    }
}