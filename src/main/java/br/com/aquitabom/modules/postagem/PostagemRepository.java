package br.com.aquitabom.modules.postagem;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostagemRepository extends JpaRepository<Postagem, UUID> {

    @EntityGraph(attributePaths = {"usuario", "restaurante"})
    List<Postagem> findAllByOrderByDataCriacaoDesc();

    @EntityGraph(attributePaths = {"usuario", "restaurante"})
    List<Postagem> findByRestauranteIdOrderByDataCriacaoDesc(UUID restauranteId);

    List<Postagem> findAllByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Postagem p SET p.likes = p.likes + 1 WHERE p.id = :id")
    void incrementarLikes(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Postagem p SET p.likes = p.likes - 1 WHERE p.id = :id AND p.likes > 0")
    void decrementarLikes(@Param("id") UUID id);
}
