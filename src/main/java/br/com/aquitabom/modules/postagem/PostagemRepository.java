package br.com.aquitabom.modules.postagem;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostagemRepository extends JpaRepository<Postagem, UUID> {

    @EntityGraph(attributePaths = {"usuario", "restaurante"})
    List<Postagem> findAllByOrderByDataCriacaoDesc();

    @EntityGraph(attributePaths = {"usuario", "restaurante"})
    List<Postagem> findByRestauranteIdOrderByDataCriacaoDesc(UUID restauranteId);

    List<Postagem> findAllByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);


}