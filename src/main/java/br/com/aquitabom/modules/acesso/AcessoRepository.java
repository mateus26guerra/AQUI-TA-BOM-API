package br.com.aquitabom.modules.acesso;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AcessoRepository extends JpaRepository<Acesso, UUID> {

    Optional<Acesso> findByNome(String nome);
}
