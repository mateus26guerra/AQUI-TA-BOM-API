package br.com.aquitabom.modules.curtida;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface CurtidaRepository extends JpaRepository<Curtida, UUID> {

    boolean existsByPostagemIdAndUsuarioId(UUID postagemId, UUID usuarioId);

    long deleteByPostagemIdAndUsuarioId(UUID postagemId, UUID usuarioId);

    long deleteByPostagemId(UUID postagemId);

    List<Curtida> findTop2ByPostagemIdOrderByDataCriacaoAsc(UUID postagemId);

    List<Curtida> findTop2ByPostagemIdOrderByDataCriacaoDesc(UUID postagemId);

    @EntityGraph(attributePaths = {"usuario", "postagem", "postagem.restaurante"})
    List<Curtida> findByPostagemIdOrderByDataCriacaoDesc(UUID postagemId);

    @EntityGraph(attributePaths = {"usuario", "postagem", "postagem.restaurante"})
    List<Curtida> findByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);

    @EntityGraph(attributePaths = {"usuario", "postagem", "postagem.restaurante"})
    List<Curtida> findAllByOrderByDataCriacaoDesc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                DELETE FROM Curtida x
                WHERE x.postagem.id IN (
                    SELECT p.id FROM Postagem p WHERE p.restaurante.id = :restauranteId
                )
            """)
    void deleteByPostagemRestauranteId(@Param("restauranteId") UUID restauranteId);
}
