package br.com.aquitabom.modules.avaliacaorestaurante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoRestauranteRepository
        extends JpaRepository<AvaliacaoRestaurante, UUID> {

    Optional<AvaliacaoRestaurante>
    findByUsuarioIdAndRestauranteId(
            UUID usuarioId,
            UUID restauranteId
    );

    List<AvaliacaoRestaurante>
    findByRestauranteIdOrderByDataCriacaoDesc(
            UUID restauranteId
    );

    long countByRestauranteId(UUID restauranteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AvaliacaoRestaurante x WHERE x.restaurante.id = :restauranteId")
    void deleteByRestauranteId(@Param("restauranteId") UUID restauranteId);
}
