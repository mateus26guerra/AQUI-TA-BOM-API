package br.com.aquitabom.modules.avaliacaorestaurante;

import org.springframework.data.jpa.repository.JpaRepository;

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
}