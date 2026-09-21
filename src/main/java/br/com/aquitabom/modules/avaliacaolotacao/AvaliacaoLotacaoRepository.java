package br.com.aquitabom.modules.avaliacaolotacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoLotacaoRepository
        extends JpaRepository<AvaliacaoLotacao, UUID> {

    Optional<AvaliacaoLotacao> findByUsuarioIdAndRestauranteId(
            UUID usuarioId,
            UUID restauranteId
    );

    List<AvaliacaoLotacao> findByRestauranteIdAndDataCriacaoAfter(
            UUID restauranteId,
            OffsetDateTime data
    );
}