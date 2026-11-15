package br.com.aquitabom.modules.avaliacaolotacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AvaliacaoLotacao x WHERE x.restaurante.id = :restauranteId")
    void deleteByRestauranteId(@Param("restauranteId") UUID restauranteId);
}
