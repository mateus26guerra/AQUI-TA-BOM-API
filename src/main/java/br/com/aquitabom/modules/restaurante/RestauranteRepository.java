package br.com.aquitabom.modules.restaurante;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RestauranteRepository extends JpaRepository<Restaurante, UUID> {
}