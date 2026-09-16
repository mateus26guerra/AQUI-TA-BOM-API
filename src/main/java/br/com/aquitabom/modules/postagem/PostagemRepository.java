package br.com.aquitabom.modules.postagem;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostagemRepository extends JpaRepository<Postagem, UUID> {

    /**
     * Busca todas as postagens ordenadas da mais recente para a mais antiga.
     * O @EntityGraph realiza um JOIN FETCH trazendo 'usuario' e 'restaurante'
     * em uma única query SQL, evitando o problema de N+1.
     */
    @EntityGraph(attributePaths = {"usuario", "restaurante"})
    List<Postagem> findAllByOrderByDataCriacaoDesc();

    /**
     * Lista postagens de um restaurante específico ordenadas por data.
     */
    @EntityGraph(attributePaths = {"usuario", "restaurante"})
    List<Postagem> findByRestauranteIdOrderByDataCriacaoDesc(UUID restauranteId);
}