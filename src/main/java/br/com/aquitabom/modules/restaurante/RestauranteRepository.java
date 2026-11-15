package br.com.aquitabom.modules.restaurante;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RestauranteRepository extends JpaRepository<Restaurante, UUID> {

    @EntityGraph(attributePaths = "formasPagamento")
    @Query("SELECT r FROM Restaurante r")
    List<Restaurante> findAllComFormasPagamento();
}