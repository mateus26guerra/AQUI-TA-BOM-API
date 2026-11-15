package br.com.aquitabom.modules.comentario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface ComentarioRepository extends JpaRepository<Comentario, UUID> {

    List<Comentario> findByPostagemIdOrderByDataCriacaoAsc(UUID postagemId);

    List<Comentario> findByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);

    long deleteByPostagemId(UUID postagemId);
    long deleteByIdAndUsuarioId(UUID comentarioId, UUID usuarioId);
    long countByPostagemId(UUID postagemId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                DELETE FROM Comentario x
                WHERE x.postagem.id IN (
                    SELECT p.id FROM Postagem p WHERE p.restaurante.id = :restauranteId
                )
            """)
    void deleteByPostagemRestauranteId(@Param("restauranteId") UUID restauranteId);
}
