package br.com.aquitabom.modules.LotacaoRestaurante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LotacaoRestauranteRepository
        extends JpaRepository<LotacaoRestaurante, UUID> {

    Optional<LotacaoRestaurante> findByRestauranteId(UUID restauranteId);
}