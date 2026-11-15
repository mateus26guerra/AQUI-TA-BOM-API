package br.com.aquitabom.modules.LotacaoRestaurante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;
import java.util.UUID;

public interface LotacaoRestauranteRepository
        extends JpaRepository<LotacaoRestaurante, UUID> {

    Optional<LotacaoRestaurante> findByRestauranteId(UUID restauranteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LotacaoRestaurante x WHERE x.restaurante.id = :restauranteId")
    void deleteByRestauranteId(@Param("restauranteId") UUID restauranteId);
}
